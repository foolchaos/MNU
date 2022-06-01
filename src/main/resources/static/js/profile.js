document.getElementById('change-password-button').addEventListener('click', e => {
    e.target.classList.add('hidden');
    document.getElementById('change-password-form').classList.remove('hidden');
});