document.addEventListener('click', (e) => {
    const requestId = e.target.dataset.requestId;
    const action = requestId && e.target.dataset.action;
    const role = document.getElementsByTagName("body")[0].dataset.role;

    if (action === "accept") {
        fetch(`/${role}/acceptJobApplication/` + requestId, {method: 'POST'})
            .then(() => {
                document.getElementById("request-buttons-" + requestId).classList.add("hidden");
                document.getElementById("request-flash-" + requestId).classList.remove('hidden');
                document.getElementById("request-content-" + requestId).classList.add('approved');
                document.getElementById("request-content-" + requestId).classList.remove('finished');
                document.getElementById("request-flash-" + requestId).innerHTML = "Accepted";
            })
    } else if (action === "reject") {
        fetch(`/${role}/rejectJobApplication/` + requestId, {method: 'POST'})
            .then(() => {
                document.getElementById("request-buttons-" + requestId).classList.add("hidden");
                document.getElementById("request-flash-" + requestId).classList.remove('hidden');
                document.getElementById("request-content-" + requestId).classList.add('rejected');
                document.getElementById("request-content-" + requestId).classList.remove('finished');
                document.getElementById("request-flash-" + requestId).innerHTML = "Rejected";
            })
    }
});