dealWithBadJobs();

document.getElementById("sorter").addEventListener('change', () => {
    document.getElementById('submit-sort-button').click();
});

document.getElementById('hider').addEventListener('change', dealWithBadJobs);

function dealWithBadJobs() {
    const hider = document.getElementById("hider");
    let badJobs = document.querySelectorAll('.vacancy-item.rejected');
    for (let i = 0; i < badJobs.length; ++i) {
        if (hider.checked) badJobs[i].classList.add("hidden");
        else badJobs[i].classList.remove('hidden');
    }
}