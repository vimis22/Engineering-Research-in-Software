#!/usr/bin/env python3
"""Runner script for the IwanDroid hybrid app IFC analysis pipeline."""

from __future__ import annotations

import logging
import os
import re
import subprocess
import sys
from argparse import ArgumentParser, RawTextHelpFormatter
from dataclasses import dataclass
from pathlib import Path
from time import perf_counter

logger = logging.getLogger(__name__)

TIMEOUT_SECONDS = 1000
JVM_HEAP_SIZE = "16G"
JS_CODE_DIR = Path("JSCode")
PREPROCESSING_JAR = Path("PreprocessingBinary", "Preprocessing.jar")
IFC_JAR = Path("IFCAnalysis", "target", "iwanDroid-1.0-SNAPSHOT-jar-with-dependencies.jar")


@dataclass
class AnalysisConfig:
    app_name: str
    apk_file: Path
    js_dir: Path
    js_file_path: str
    api_level: int
    android_jar_path: Path
    db_path: Path
    susi_file: Path

    def to_properties(self) -> dict[str, str]:
        return {
            "appName": self.app_name,
            "apkFile": str(self.apk_file),
            "jsDir": str(self.js_dir),
            "jsFilePath": self.js_file_path,
            "apiLevel": str(self.api_level),
            "androidJarPath": str(self.android_jar_path),
            "dbPath": str(self.db_path),
            "susiFile": str(self.susi_file),
        }

    def write(self, directory: Path) -> Path:
        filepath = directory / f"{self.app_name}.prop"
        filepath.write_text(
            "".join(f"{k}={v}\n" for k, v in self.to_properties().items())
        )
        logger.info("Created config file %s", filepath)
        return filepath


def _require_path(path: Path, description: str) -> None:
    if not path.exists():
        sys.exit(f"Error: {description} not found: {path}")


def make_config(
    app_name: str,
    apk_file: Path,
    js_dir: Path,
    js_script: str,
    database: Path,
    susi_file: Path,
    android_sdk_root: Path,
    version: int,
) -> AnalysisConfig:
    android_jar = android_sdk_root / "platforms" / f"android-{version}" / "android.jar"

    for path, desc in [
        (android_jar, "android.jar"),
        (apk_file, "APK file"),
        (susi_file, "Sources/sinks file"),
        (database, "Database"),
        (js_dir, "JS directory"),
    ]:
        _require_path(path, desc)

    js_full = js_dir / js_script
    if not js_full.exists():
        logger.warning("JS file not found: %s", js_full)

    return AnalysisConfig(
        app_name=app_name,
        apk_file=apk_file,
        js_dir=js_dir,
        js_file_path=js_script,
        api_level=version,
        android_jar_path=android_jar,
        db_path=database,
        susi_file=susi_file,
    )


def _maven_build() -> None:
    """Build the project with Maven if the IFC JAR is missing."""
    if IFC_JAR.exists():
        return
    logger.info("IFC JAR not found, running Maven build...")
    try:
        subprocess.run(
            ["mvn", "-q", "package", "-DskipTests"],
            timeout=TIMEOUT_SECONDS, check=True,
        )
    except FileNotFoundError:
        sys.exit("Error: 'mvn' not found. Install Maven or build manually.")
    except subprocess.CalledProcessError as exc:
        sys.exit(f"Error: Maven build failed (exit code {exc.returncode})")
    _require_path(IFC_JAR, "IFC JAR after build")


def _run_jar(jar: Path, args: list[str], *, logfile: Path | None = None) -> None:
    _require_path(jar, f"JAR file {jar}")
    command = ["java", f"-Xmx{JVM_HEAP_SIZE}", "-jar", str(jar), *args]
    logger.info("Running: %s", " ".join(command))
    try:
        if logfile:
            with logfile.open("w") as f:
                subprocess.run(command, timeout=TIMEOUT_SECONDS, stdout=f, stderr=f, check=False)
        else:
            subprocess.run(command, timeout=TIMEOUT_SECONDS, check=False)
    except subprocess.TimeoutExpired:
        logger.error("Command timed out after %ds", TIMEOUT_SECONDS)


def run_pre_processing(apps_path: Path, log_dir: Path | None = None) -> None:
    args = [str(apps_path)]
    if log_dir is not None:
        args.extend(["-l", str(log_dir)])
    _run_jar(PREPROCESSING_JAR, args)


def scan_apks(root_dir: Path):
    """Yield (apk_name, apk_path) for every .apk under root_dir."""
    yield from (
        (path.name, path)
        for path in root_dir.rglob("*.apk")
    )


def find_js_file(js_dir: Path, apk_stem: str) -> str | None:
    """Return the first JS filename whose name starts with the APK stem."""
    for path in js_dir.rglob("*"):
        if path.is_file() and path.name.startswith(apk_stem):
            return path.name
    return None


def construct_js_dir(js_root: Path, apk_stem: str) -> Path:
    canonical = re.sub(r"_\d+", "", apk_stem)
    return js_root / canonical


def run_ifc(
    apps_directory: Path,
    database: Path,
    susi_file: Path,
    android_sdk_root: Path,
    version: int,
) -> None:
    logs_dir = apps_directory / "logs_new"
    logs_dir.mkdir(exist_ok=True)

    for apk_name, apk_path in scan_apks(apps_directory):
        apk_stem = apk_path.stem
        js_file = find_js_file(JS_CODE_DIR, apk_stem)

        if js_file is None:
            logger.warning("No JS files found for %s, skipping", apk_name)
            continue

        config = make_config(
            apk_stem, apk_path, JS_CODE_DIR, js_file,
            database, susi_file, android_sdk_root, version,
        )

        for k, v in config.to_properties().items():
            logger.info("%s=%s", k, v)

        logger.info("Initiating IFC analysis for %s", apk_stem)
        config_file = config.write(apps_directory)

        start = perf_counter()
        _run_jar(IFC_JAR, ["-p", str(config_file)], logfile=logs_dir / f"{apk_name}.log")
        elapsed = perf_counter() - start

        logger.info("Finished %s in %.2f min", apk_stem, elapsed / 60)


def validate_args(args) -> None:
    errors = []
    if not Path(args.susi_file).exists():
        errors.append(f"Sources/sinks file not found: {args.susi_file}")
    if not Path(args.database).exists():
        errors.append(f"Database not found: {args.database}")
    if not Path(args.apps_directory).exists():
        errors.append(f"APK directory not found: {args.apps_directory}")
    if args.android_sdk_root is None or not Path(args.android_sdk_root).exists():
        errors.append("Set ANDROID_SDK_ROOT or provide a valid path with -l")
    if errors:
        sys.exit("\n".join(errors))


def parse_args(argv: list[str] | None = None):
    parser = ArgumentParser("iwandroid", formatter_class=RawTextHelpFormatter)
    parser.add_argument(
        "-d", dest="database", default=str(Path("HybridAppAnalysis", "Database", "Intent.sqlite")),
        help="database path from pre-processing (default: HybridAppAnalysis/Database/Intent.sqlite)",
    )
    parser.add_argument(
        "-apks", dest="apps_directory", required=True,
        help="directory containing android APK files",
    )
    parser.add_argument(
        "-s", dest="susi_file",
        default=str(Path("IFCAnalysis", "resource", "SourcesAndSinks.txt")),
        help="sources/sinks file (default: IFCAnalysis/resource/SourcesAndSinks.txt)",
    )
    parser.add_argument(
        "-l", dest="android_sdk_root",
        default=os.environ.get("ANDROID_SDK_ROOT"),
        help="path to Android SDK root",
    )
    parser.add_argument(
        "-v", dest="version", type=int, default=30,
        help="android API level (default: 30)",
    )
    parser.add_argument(
        "--log-dir", dest="log_dir", default=None,
        help="directory for preprocessing log files (default: output/logs)",
    )
    return parser.parse_args(argv)


def main(argv: list[str] | None = None) -> None:
    logging.basicConfig(
        level=logging.INFO,
        format="%(levelname)s: %(message)s",
    )

    args = parse_args(argv)
    validate_args(args)

    apps_dir = Path(args.apps_directory)
    log_dir = Path(args.log_dir) if args.log_dir else None

    _maven_build()
    run_pre_processing(apps_dir, log_dir=log_dir)
    run_ifc(
        apps_dir,
        Path(args.database),
        Path(args.susi_file),
        Path(args.android_sdk_root),
        args.version,
    )


if __name__ == "__main__":
    main()
