// import Vue from "vue";

let websocket;

$(document).ready(function () {
    websocket = new WebSocket("ws://" + location.hostname + ":9000/websocket");
    connectWebSocket(handle)
})

function connectWebSocket(handleData) {
    websocket.onopen = (event) => {
        console.log("Connected to Websocket");
        let msg = {
            "connect": "success"
        }
        websocket.send(JSON.stringify(msg));
    };

    websocket.onclose = () => {
        console.log("Connection with Websocket Closed!");
    };

    websocket.onerror = (error) => {
        console.log("Error in Websocket Occured: " + error);
        websocket = new WebSocket("ws://" + location.hostname + ":9000/websocket");
        connectWebSocket(handle)
    };

    websocket.onmessage = (e) => {
        if (typeof e.data === "string") {
            handleData(e.data)
        }
    };
}

function handle(rawData) {
    let json = JSON.parse(rawData);
    let newGame = json['newGame'];
    if (newGame != null) {
        console.log("got data from boardgame " + json.data)
        location.href = '/gameBoard';
    }
}

const AttributeBinding = {
    data() {
        return {
            message: 'You loaded this page on ' + new Date().toLocaleString()
        }
    }
}

Vue.createApp(AttributeBinding).mount('#bind-attribute')