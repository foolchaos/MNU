document.getElementById("burger").addEventListener('click', e => {
    if (document.getElementById('mobile-menu').style.display === 'none' || document.getElementById('mobile-menu').style.display === "") {
        document.getElementById('mobile-menu').style.display = 'flex';
    } else {
        document.getElementById('mobile-menu').style.display = 'none';
    }
});