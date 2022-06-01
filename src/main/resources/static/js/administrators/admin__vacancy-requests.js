document.addEventListener('click', (e) => {
    const requestId = e.target.dataset.requestId;
    const action = requestId && e.target.dataset.action;

    if (action === "accept") {

        fetch("/admin/acceptNewVacancy/" + requestId, {method: 'POST'})
            .then(() => {
                dealWithRequest(true, requestId);
            })
    } else if (action === "reject") {
        fetch("/admin/rejectNewVacancy/" + requestId, {method: 'POST'})
            .then(() => {
                dealWithRequest(false, requestId);
            })
    }
});

function dealWithRequest(isAccept, requestId) {
    document.getElementById("request-buttons-" + requestId).classList.add("hidden");
    document.getElementById("request-flash-" + requestId).classList.remove('hidden');
    if (isAccept) {
        document.getElementById("request-content-" + requestId).classList.add('approved');
        document.getElementById("request-content-" + requestId).classList.remove('light-gray-bg');
        document.getElementById("request-content-" + requestId).classList.remove('rejected');
        document.getElementById("request-flash-message-" + requestId).innerHTML = "Accepted";
    } else {
        document.getElementById("request-content-" + requestId).classList.add('rejected');
        document.getElementById("request-content-" + requestId).classList.remove('light-gray-bg');
        document.getElementById("request-content-" + requestId).classList.remove('approved');
        document.getElementById("request-flash-message-" + requestId).innerHTML = "Rejected";
    }
    let timeLeft = 9;
    let undoButton = document.getElementById("request-undo-" + requestId);

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
        fetch("/admin/undoVacancyChoice/" + requestId, {method: 'POST'})
            .then(() => {
                document.getElementById("request-buttons-" + requestId).classList.remove("hidden");
                document.getElementById("request-flash-" + requestId).classList.add('hidden');
                document.getElementById("request-content-" + requestId).classList.add('light-gray-bg');
                document.getElementById("request-content-" + requestId).classList.remove('approved');
                document.getElementById("request-content-" + requestId).classList.remove('rejected');
                undoButton.innerHTML = "Undo 10";
            })
    });
}