const modeSwitch = document.getElementById('modeSwitch');
modeSwitch.addEventListener('change', () => {
    document.body.classList.toggle('dark-mode');
    document.body.classList.toggle('light-mode');
    if (modeSwitch.checked) {
        modeSwitch.nextElementSibling.textContent = 'Light Mode';
    } else {
        modeSwitch.nextElementSibling.textContent = 'Dark Mode';
    }
});

const eventsTitle = document.getElementById('eventsTitle');

modeSwitch.addEventListener('change', () => {
    if (modeSwitch.checked) {
        eventsTitle.classList.remove('dark-mode');
        eventsTitle.classList.add('light-mode');
    } else {
        eventsTitle.classList.remove('light-mode');
        eventsTitle.classList.add('dark-mode');
    }
});
const searchInput = document.getElementById('searchInput');

modeSwitch.addEventListener('change', () => {
    if (modeSwitch.checked) {
        searchInput.classList.remove('dark-mode');
        searchInput.classList.add('light-mode');
    } else {
        searchInput.classList.remove('light-mode');
        searchInput.classList.add('dark-mode');
    }
});
const eventsCards = document.querySelectorAll('.card');

modeSwitch.addEventListener('change', () => {
    eventsCards.forEach(card => {
        if (modeSwitch.checked) {
            card.classList.remove('dark-mode');
            card.classList.add('light-mode');
        } else {
            card.classList.remove('light-mode');
            card.classList.add('dark-mode');
        }
    });
});
