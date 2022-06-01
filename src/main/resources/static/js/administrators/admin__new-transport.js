document.addEventListener('click', (e) => {
    const transportId = e.target.dataset.transportId;
    const action = transportId && e.target.dataset.action;

    if (action === "accept") {
        fetch(`/admin/acceptNewTransport/` + transportId, {method: 'POST'})
            .then(() => {
                dealWithTransport(true, transportId);
            })
    } else if (action === "reject") {
        fetch(`/admin/rejectNewTransport/` + transportId, {method: 'POST'})
            .then(() => {
                dealWithTransport(false, transportId);
            })
    }
});

function dealWithTransport(isAccept, transportId) {
    document.getElementById("transport-buttons-" + transportId).classList.add("hidden");
    document.getElementById("transport-flash-" + transportId).classList.remove('hidden');
    if (isAccept) {
        document.getElementById("transport-content-" + transportId).classList.add('approved');
        document.getElementById("transport-content-" + transportId).classList.remove('finished');
        document.getElementById("transport-content-" + transportId).classList.remove('rejected');
        document.getElementById("transport-flash-message-" + transportId).innerHTML = "Accepted";
    } else {
        document.getElementById("transport-content-" + transportId).classList.add('rejected');
        document.getElementById("transport-content-" + transportId).classList.remove('finished');
        document.getElementById("transport-content-" + transportId).classList.remove('approved');
        document.getElementById("transport-flash-message-" + transportId).innerHTML = "Rejected";
    }
    let timeLeft = 9;
    let undoButton = document.getElementById("transport-undo-" + transportId);

    let timer = setInterval(() => {
        undoButton.innerHTML = "Undo " + timeLeft;
        timeLeft--;
    }, 1000);
    let timeout = setTimeout(() => {
        clearInterval(timer);
        undoButton.parentNode.removeChild(undoButton);
    }, 10000);
    undoButton.addEventListener('click', () => {
        clearInterval(timer);
        clearTimeout(timeout);
        fetch("/admin/undoTransportChoice/" + transportId, {method: 'POST'})
            .then(() => {
                document.getElementById("transport-buttons-" + transportId).classList.remove("hidden");
                document.getElementById("transport-flash-" + transportId).classList.add('hidden');
                document.getElementById("transport-content-" + transportId).classList.add('finished');
                document.getElementById("transport-content-" + transportId).classList.remove('approved');
                document.getElementById("transport-content-" + transportId).classList.remove('rejected');
                undoButton.innerHTML = "Undo 10";
            })
    });
}