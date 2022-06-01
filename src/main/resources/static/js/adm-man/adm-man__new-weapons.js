document.addEventListener('click', (e) => {
    const weaponId = e.target.dataset.weaponId;
    const action = weaponId && e.target.dataset.action;
    const role = document.getElementsByTagName("body")[0].dataset.role;

    if (action === "accept") {
        fetch(`/${role}/acceptNewWeapon/` + weaponId, {method: 'POST'})
            .then(() => {
                dealWithWeapon(true, weaponId, role);
            })
    } else if (action === "reject") {
        fetch(`/${role}/rejectNewWeapon/` + weaponId, {method: 'POST'})
            .then(() => {
                dealWithWeapon(false, weaponId, role);
            })
    }
});

function dealWithWeapon(isAccept, weaponId, role) {
    document.getElementById("weapon-buttons-" + weaponId).classList.add("hidden");
    document.getElementById("weapon-flash-" + weaponId).classList.remove('hidden');
    if (isAccept) {
        document.getElementById("weapon-content-" + weaponId).classList.add('approved');
        document.getElementById("weapon-content-" + weaponId).classList.remove('finished');
        document.getElementById("weapon-content-" + weaponId).classList.remove('rejected');
        document.getElementById("weapon-flash-message-" + weaponId).innerHTML = "Accepted";
    } else {
        document.getElementById("weapon-content-" + weaponId).classList.add('rejected');
        document.getElementById("weapon-content-" + weaponId).classList.remove('finished');
        document.getElementById("weapon-content-" + weaponId).classList.remove('approved');
        document.getElementById("weapon-flash-message-" + weaponId).innerHTML = "Rejected";
    }
    let timeLeft = 9;
    let undoButton = document.getElementById("weapon-undo-" + weaponId);

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
        fetch(`/${role}/undoWeaponChoice/` + weaponId, {method: 'POST'})
            .then(() => {
                document.getElementById("weapon-buttons-" + weaponId).classList.remove("hidden");
                document.getElementById("weapon-flash-" + weaponId).classList.add('hidden');
                document.getElementById("weapon-content-" + weaponId).classList.add('finished');
                document.getElementById("weapon-content-" + weaponId).classList.remove('approved');
                document.getElementById("weapon-content-" + weaponId).classList.remove('rejected');
                undoButton.innerHTML = "Undo 10";
            })
    });
}