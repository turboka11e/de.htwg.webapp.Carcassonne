// import Vue from "vue";
const AttributeBinding = {
    data() {
        return {
            connection: new WebSocket("ws://" + location.hostname + ":9000/websocket"),
            readyState: 3,
            newGame: true,
            gameModel: "#newGameModel",
            players: [],
        }
    },
    methods: {
        newWebsocket() {
            console.log("Connecting to WebSocket...");

            this.connection.onopen = (event) => {
                this.readyState = this.connection.readyState
                console.log("Connected to Websocket");
                let msg = {
                    "homescreen": "success"
                }
                this.connection.send(JSON.stringify(msg));
            };
            this.connection.onclose = () => {
                this.readyState = this.connection.readyState
                console.log("Connection with Websocket Closed!");
            };

            this.connection.onerror = (error) => {
                this.readyState = this.connection.readyState
                console.log("Error in Websocket Occured: " + error);
                this.newWebsocket()
            };

            this.connection.onmessage = (e) => {
                this.readyState = this.connection.readyState
                console.log("received message " + e)

                if (typeof e.data === "string") {
                    let json = JSON.parse(e.data)

                    for (let [key, value] of Object.entries(json)) {
                        if (key === 'joinGame') {
                            this.gameModel = "#joinGameModel"
                            this.newGame = false;
                            this.players = value;
                            console.log(value);
                        }
                    }
                }
            };
        },
    },
    computed: {
        websocketState() {
            switch (this.readyState) {
                case 0:
                    return "CONNECTING"
                case 1:
                    return "OPEN"
                case 2:
                    return "CLOSING"
                case 3:
                    return "CLOSED"
            }
        },
        websocketStateColor() {
            switch (this.readyState) {
                case 0:
                    return "bg-success"
                case 1:
                    return "bg-success"
                case 2:
                    return "bg-danger"
                case 3:
                    return "bg-danger"
            }
        },
    },
    created() {
        this.newWebsocket()
    }
}

Vue.createApp(AttributeBinding).mount('#app')