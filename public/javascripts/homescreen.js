let websocket = new WebSocket("ws://localhost:9000/websocket");
const connectWebSocket = handleData => {
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
        websocket = new WebSocket("ws://localhost:9000/websocket");
        connectWebSocket(handle)
    };

    websocket.onmessage = (e) => {
        if (typeof e.data === "string") {
            handleData(e.data)
        }
    };
};

function handle(rawData) {
    let json = JSON.parse(rawData);
    console.log("got data from boardgame " + json.data)
    location.href = '/gameBoard';
}

$(document).ready(function () {
    connectWebSocket(handle)
})