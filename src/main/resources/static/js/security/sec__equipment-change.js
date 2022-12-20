document.getElementById("equipment-switch").addEventListener('change', e => {
    if (e.target.checked) {
        document.getElementById("weapons-tbody").classList.remove("hidden");
        document.getElementById("transport-tbody").classList.add("hidden");
    } else {
        document.getElementById("weapons-tbody").classList.add("hidden");
        document.getElementById("transport-tbody").classList.remove("hidden");
    }
});

document.getElementById("weapons-tbody").addEventListener('click', e => {
    const row = e.target.closest('.weapon-row');
    if (!row) return;
    if (row.classList.contains("selected-row")) {
        row.classList.remove("selected-row");
        document.getElementById("weaponId").value = "";
    } else {
        let selectedRows = document.getElementsByClassName("selected-row");
        while (selectedRows.length > 0) selectedRows[0].classList.remove("selected-row");
        row.classList.add("selected-row");
        document.getElementById("weaponId").value = row.dataset.id;
    }
});

document.getElementById("transport-tbody").addEventListener('click', e => {
    const row = e.target.closest('.transport-row');
    if (!row) return;
    if (row.classList.contains("selected-row")) {
        row.classList.remove("selected-row");
        document.getElementById("transportId").value = "";
    } else {
        let selectedRows = document.getElementsByClassName("selected-row");
        while (selectedRows.length > 0) selectedRows[0].classList.remove("selected-row");
        row.classList.add("selected-row");
        document.getElementById("transportId").value = row.dataset.id;
    }
});

document.getElementById("weaponId").addEventListener('input', e => {
    let selectedRows = document.getElementsByClassName("selected-row");
    while (selectedRows.length > 0) selectedRows[0].classList.remove("selected-row");
    let row = document.querySelector('#weapons-tbody *[data-id="' + e.target.value + '"]');
    if (!row) return;
    row.classList.add("selected-row");
});

document.getElementById("transportId").addEventListener('input', e => {
    let selectedRows = document.getElementsByClassName("selected-row");
    while (selectedRows.length > 0) selectedRows[0].classList.remove("selected-row");
    let row = document.querySelector('#transport-tbody *[data-id="' + e.target.value + '"]');
    if (!row) return;
    row.classList.add("selected-row");
});