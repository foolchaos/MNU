document.getElementById("add-button").addEventListener('click', () => {
    pressButton(document.getElementById("add-button"));
    unhideInterface(document.getElementById("add-interface"));
});

document.getElementById("edit-button").addEventListener('click', () => {
    pressButton(document.getElementById("edit-button"));
    unhideInterface(document.getElementById("edit-interface"));
});

document.getElementById("find-button").addEventListener('click', () => {
    pressButton(document.getElementById("find-button"));
    unhideInterface(document.getElementById("find-interface"));
});

document.getElementById("reward-button").addEventListener('click', () => {
    pressButton(document.getElementById("reward-button"));
    unhideInterface(document.getElementById("reward-interface"));
});

function unhideInterface(unhidden_interface) {
    document.getElementById("add-interface").classList.add("hidden");
    document.getElementById("edit-interface").classList.add("hidden");
    document.getElementById("find-interface").classList.add("hidden");
    document.getElementById("reward-interface").classList.add("hidden");
    unhidden_interface.classList.remove("hidden");
}

function pressButton(button_to_press) {
    let pressed_buttons = document.getElementsByClassName("button_pressed");
    while (pressed_buttons.length > 0) pressed_buttons[0].classList.remove("button_pressed");
    button_to_press.classList.add("button_pressed");
}

document.getElementById("employees-tbody").addEventListener('click', e => {
    const row = e.target.closest('.employee-tbody-row');
    if (!row) return;
    if (row.classList.contains("selected-row")) {
        row.classList.remove("selected-row");
        document.getElementById("id_edit").value = "";
        document.getElementById("name_edit").value = "";
        document.getElementById("salary_edit").value = "";
        document.getElementById("position_edit").value = "";
        document.getElementById("status_edit").value = "working";
        document.getElementById("level_edit").value = "";
    } else {
        let selectedRows = document.getElementsByClassName("selected-row");
        while (selectedRows.length > 0) selectedRows[0].classList.remove("selected-row");
        row.classList.add("selected-row");
        document.getElementById("id_edit").value = row.dataset.id;
        document.getElementById("name_edit").value = row.dataset.name;
        document.getElementById("salary_edit").value = row.dataset.salary;
        document.getElementById("position_edit").value = row.dataset.position;
        document.getElementById("status_edit").value = row.dataset.status.toLowerCase();
        document.getElementById("level_edit").value = row.dataset.level;
        document.getElementById("id_cash").value = row.dataset.id;
    }
});

document.getElementById("id_edit").addEventListener('input', e => {
    let selectedRows = document.getElementsByClassName("selected-row");
    while (selectedRows.length > 0) selectedRows[0].classList.remove("selected-row");
    let row = document.querySelector('#employees-tbody *[data-id="' + e.target.value + '"]');
    if (!row) return;
    row.classList.add("selected-row");
    document.getElementById("name_edit").value = row.dataset.name;
    document.getElementById("salary_edit").value = row.dataset.salary;
    document.getElementById("position_edit").value = row.dataset.position;
    document.getElementById("status_edit").value = row.dataset.status.toLowerCase();
    document.getElementById("level_edit").value = row.dataset.level;
});

statuses = document.getElementsByClassName("emp-status");
for (let i = 0; i < statuses.length; ++i) {
    statuses[i].innerText = statuses[i].innerText.toLowerCase();
}

roles = document.getElementsByClassName("emp-role");
for (let i = 0; i < roles.length; ++i) {
    roles[i].innerText = roles[i].innerText.substr(0, 3);
}