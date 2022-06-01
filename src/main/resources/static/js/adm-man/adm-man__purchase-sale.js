document.addEventListener('click', (e) => {
    const requestId = e.target.dataset.requestId;
    const action = requestId && e.target.dataset.action;
    const baseUrl = requestId && e.target.dataset.baseurl;

    if (action === "accept") {
        fetch(baseUrl + requestId, {method: 'POST'})
            .then(() => {
                document.getElementById("request-buttons-" + requestId).classList.add("hidden");
                document.getElementById("request-flash-" + requestId).classList.remove('hidden');
                document.getElementById("request-content-" + requestId).classList.add('approved');
                document.getElementById("request-content-" + requestId).classList.remove('finished');
                document.getElementById("request-flash-" + requestId).innerHTML = "Accepted";
            })
    } else if (action === "reject") {
        fetch(baseUrl + requestId, {method: 'POST'})
            .then(() => {
                document.getElementById("request-buttons-" + requestId).classList.add("hidden");
                document.getElementById("request-flash-" + requestId).classList.remove('hidden');
                document.getElementById("request-content-" + requestId).classList.add('rejected');
                document.getElementById("request-content-" + requestId).classList.remove('finished');
                document.getElementById("request-flash-" + requestId).innerHTML = "Rejected";
            })
    }
});