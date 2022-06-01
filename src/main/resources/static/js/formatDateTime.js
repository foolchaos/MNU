datetimes = document.getElementsByClassName("datetime-to-format");
for (let i = 0; i < datetimes.length; ++i) {
    const date = new Date(datetimes[i].innerText);
    const day = ("0" + date.getDate()).slice(-2);
    const month = ("0" + (date.getMonth() + 1)).slice(-2);
    const year = date.getFullYear();
    const hours = ("0" + date.getHours()).slice(-2);
    const minutes = ("0" + date.getMinutes()).slice(-2);
    const seconds = ("0" + date.getSeconds()).slice(-2);
    datetimes[i].innerText = `${day}.${month}.${year} ${hours}:${minutes}:${seconds}`;
}

dates = document.getElementsByClassName("date-to-format");
for (let i = 0; i < dates.length; ++i) {
    const date = new Date(dates[i].innerText);
    const day = ("0" + date.getDate()).slice(-2);
    const month = ("0" + (date.getMonth() + 1)).slice(-2);
    const year = date.getFullYear();
    dates[i].innerText = `${day}.${month}.${year}`;
}