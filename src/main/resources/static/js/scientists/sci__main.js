document.addEventListener('click', (e) => {
    const experimentId = e.target.dataset.expId;
    const action = experimentId && e.target.dataset.action;

    if (action === "accept") {
        fetch("/sci/acceptExperiment/" + experimentId, {method: 'POST'})
            .then(() => {
                dealWithExperiment(true, experimentId);
            })
    } else if (action === "reject") {
        fetch("/sci/rejectExperiment/" + experimentId, {method: 'POST'})
            .then(() => {
                dealWithExperiment(false, experimentId);
            })
    }
});

function dealWithExperiment(isAccept, experimentId) {
    document.getElementById("exp-content-" + experimentId).classList.add("hidden");
    document.getElementById("exp-flash-" + experimentId).classList.remove('hidden');
    if (isAccept) {
        document.getElementById("exp-header-" + experimentId).classList.add('approved');
        document.getElementById("exp-header-" + experimentId).classList.remove('rejected');
        document.getElementById("exp-header-" + experimentId).classList.remove('pending');
        document.getElementById("exp-flash-message-" + experimentId).innerHTML = "Accepted";
    } else {
        document.getElementById("exp-header-" + experimentId).classList.add('rejected');
        document.getElementById("exp-header-" + experimentId).classList.remove('approved');
        document.getElementById("exp-header-" + experimentId).classList.remove('pending');
        document.getElementById("exp-flash-message-" + experimentId).innerHTML = "Rejected";
    }
    let timeLeft = 9;
    let undoButton = document.getElementById("exp-undo-" + experimentId);

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
        fetch("/sci/undoExperimentChoice/" + experimentId, {method: 'POST'})
            .then(() => {
                document.getElementById("exp-content-" + experimentId).classList.remove("hidden");
                document.getElementById("exp-flash-" + experimentId).classList.add('hidden');
                document.getElementById("exp-header-" + experimentId).classList.add('pending');
                document.getElementById("exp-header-" + experimentId).classList.remove('approved');
                document.getElementById("exp-header-" + experimentId).classList.remove('rejected');
                undoButton.innerHTML = "Undo 10";
            })
    });
}