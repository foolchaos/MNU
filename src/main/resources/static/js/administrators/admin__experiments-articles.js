document.addEventListener('click', async (e) => {
    const experimentId = e.target.dataset.expId;
    const empId = e.target.dataset.empid;
    const action = (experimentId || empId) && e.target.dataset.action;

    if (action === "accept") {
        fetch("/admin/acceptExperiment/" + experimentId, {method: 'POST'})
            .then(() => {
                dealWithExperiment(true, experimentId);
            })
    } else if (action === "reject") {
        fetch("/admin/rejectExperiment/" + experimentId, {method: 'POST'})
            .then(() => {
                dealWithExperiment(false, experimentId);
            })
    } else if (action === "reward") {
        let rewardInput = document.getElementById(`reward-input-${empId}`);
        if (rewardInput.classList.contains('hidden')) {
            rewardInput.classList.remove('hidden');
        } else {
            const response = await post({employeeId: empId, reward: rewardInput.value});
            const json = await response.json();
            if (json.error) {
                document.getElementById(`reward-message-${empId}`).classList.remove('hidden');
                document.getElementById(`reward-message-${empId}`).classList.add('rejected');
                document.getElementById(`reward-message-${empId}`).innerText = json.message;
            } else {
                document.getElementById(`reward-form-${empId}`).classList.add('hidden');
                document.getElementById(`reward-message-${empId}`).classList.remove('hidden');
                document.getElementById(`reward-message-${empId}`).classList.remove('rejected');
                document.getElementById(`reward-message-${empId}`).classList.add('approved');
                document.getElementById(`reward-message-${empId}`).innerText = json.message;
            }
        }
    }
});

function post(json) {
    return fetch("/admin/giveRewardAjax", {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(json)
    })
}

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
        fetch("/admin/undoExperimentChoice/" + experimentId, {method: 'POST'})
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