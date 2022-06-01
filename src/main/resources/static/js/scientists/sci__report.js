document.getElementById("isSynthesized").addEventListener('change', e => {
    const choice = e.target.value;
    if (choice === "0") {
        document.getElementById("weapon-id-label").classList.add("hidden");
        document.getElementById("weapon-quantity-label").classList.add("hidden");
        document.getElementById("weapons-table").classList.add("hidden");
        document.getElementById("weapons-form").classList.add("hidden");
    } else if (choice === "1") {
        document.getElementById("weapon-id-label").classList.remove("hidden");
        document.getElementById("weapon-quantity-label").classList.remove("hidden");
        document.getElementById("weapons-table").classList.remove("hidden");
        document.getElementById("weapons-form").classList.add("hidden");
    } else if (choice === "2") {
        document.getElementById("weapon-id-label").classList.add("hidden");
        document.getElementById("weapon-quantity-label").classList.add("hidden");
        document.getElementById("weapons-table").classList.add("hidden");
        document.getElementById("weapons-form").classList.remove("hidden");
    }
});

document.getElementById("weapons-tbody").addEventListener('click', (e) => {
    const row = e.target.closest('[data-id]');
    if (!row) return;

    const oldId = document.getElementById("weaponId").value;
    const newId = row.dataset.id;

    if (oldId !== newId) {
        let selectedRows = document.getElementsByClassName("selected-row");
        while (selectedRows.length > 0) selectedRows[0].classList.remove("selected-row");
        row.classList.add("selected-row");
        document.getElementById("weaponId").value = newId;
    } else {
        row.classList.remove("selected-row");
        document.getElementById("weaponId").value = "";
    }
});

document.getElementById("weaponId").addEventListener('input', e => {
    let selectedRows = document.getElementsByClassName("selected-row");
    while (selectedRows.length > 0) selectedRows[0].classList.remove("selected-row");
    let row = document.querySelector('#weapons-tbody *[data-id="' + e.target.value + '"]');
    if (!row) return;
    row.classList.add("selected-row");
});