function hello1() {
    console.log("hello1");
}

function hello2() {
    console.log("hello2");
}

function tobd() {
    console.log("function will be deleted")
}

function main() {
    const x = {
        printHello: hello1,
        name: "sell"
    };
    x.printHello();
    x.printHello = hello2;
    x.tdb = tobd;
    const callhello = x.printHello;
    callhello();
    delete x.tdb;
}
main()

