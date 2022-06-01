document.addEventListener('click', (e) => {
    const requestId = e.target.dataset.requestId;
    const action = requestId && e.target.dataset.action;

    if (action === "accept") {
        fetch(`/man/acceptNewEquipment/` + requestId, {method: 'POST'})
            .then(() => {
                document.getElementById("request-buttons-" + requestId).classList.add("hidden");
                document.getElementById("request-flash-" + requestId).classList.remove('hidden');
                document.getElementById("request-content-" + requestId).classList.add('approved');
                document.getElementById("request-content-" + requestId).classList.remove('finished');
                document.getElementById("request-flash-" + requestId).innerHTML = "Accepted";
            })
    } else if (action === "reject") {
        fetch(`/man/rejectNewEquipment/` + requestId, {method: 'POST'})
            .then(() => {
                document.getElementById("request-buttons-" + requestId).classList.add("hidden");
                document.getElementById("request-flash-" + requestId).classList.remove('hidden');
                document.getElementById("request-content-" + requestId).classList.add('rejected');
                document.getElementById("request-content-" + requestId).classList.remove('finished');
                document.getElementById("request-flash-" + requestId).innerHTML = "Rejected";
            })
    }
});