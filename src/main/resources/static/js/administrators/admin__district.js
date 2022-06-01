gridCircles = document.getElementsByClassName("clickable-circle");

for (let i = 0; i < gridCircles.length; ++i) {
    gridCircles[i].addEventListener('click', e => {
        const clickedCircle = document.getElementsByClassName("clicked-circle")[0];
        if (clickedCircle !== undefined) {
            clickedCircle.classList.add("clickable-circle");
            clickedCircle.classList.remove("clicked-circle");
        }
        e.target.classList.add("clicked-circle");
        e.target.classList.remove("clickable-circle");
        switch (e.target.getAttribute("fill")) {
            case "yellow":
                document.getElementById("incident-circle").className = "danger-circle danger-level-1";
                break;
            case "orange":
                document.getElementById("incident-circle").className = "danger-circle danger-level-2";
                break;
            case "red":
                document.getElementById("incident-circle").className = "danger-circle danger-level-3";
                break;
        }
        document.getElementById("incident-description").innerText = e.target.dataset.description;
        document.getElementById("incident-house").innerText = "House: " + e.target.dataset.house;
        document.getElementById("incident-date").innerText = "Appearance date: " + formatDateTime(new Date(e.target.dataset.date));
        document.getElementById("incidentId").value = e.target.dataset.id;
    });
}

function formatDateTime(date) {
    const day = ("0" + date.getDate()).slice(-2);
    const month = ("0" + (date.getMonth() + 1)).slice(-2);
    const hours = ("0" + date.getHours()).slice(-2);
    const minutes = ("0" + date.getMinutes()).slice(-2);
    const seconds = ("0" + date.getSeconds()).slice(-2);
    return `${day}.${month} ${hours}:${minutes}:${seconds}`;
}