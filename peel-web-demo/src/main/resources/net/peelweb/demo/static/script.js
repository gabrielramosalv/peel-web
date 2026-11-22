let hello = document.querySelector("#hello");

let counter = 0;
setInterval(() => {
    if (counter > 9) {
        hello.innerHTML = "Hello, World!"
    } else {
        counter++;
        hello.innerHTML = hello.innerHTML + " " + counter
    }
}, 1000)