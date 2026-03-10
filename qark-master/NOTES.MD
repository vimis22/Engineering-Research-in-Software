### Anne's Notes ###
Install python3-venv package (may need sudo):
apt install python3.12-venv

Create virtual environment:
python3 -m venv venv

Activate virtual environment (bash):
source venv/bin/activate

Install qark:
pip install qark

Also, make sure Java is installed! Might cause trouble with dex2jar otherwise.

I've downloaded these APKs, and stored them in the myApks-folder:
https://f-droid.org/en/packages/org.billthefarmer.gridle/

Run the tool for an APK:
qark --apk qark-master/myApks/name_of_apk_file.apk

I ran the tool for the gridle APK (gridle_122.apk), and results where stored in:
qark-master\venv\lib\python3.12\site-packages\qark\report\report.html