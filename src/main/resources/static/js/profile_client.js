document.getElementById('change-password-button').addEventListener('click', e => {
    e.target.classList.add('hidden');
    document.getElementById('change-password-form').classList.remove('hidden');
});

document.getElementById('change-email-button').addEventListener('click', e => {
    e.target.classList.add('hidden');
    document.getElementById('change-email-form').classList.remove('hidden');
});