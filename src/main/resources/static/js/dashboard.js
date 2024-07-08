/*  global variables for customer related information */
var customer = null;
var customerID = null;
var customerAccounts = [];
/*  Initialize a default account until another is selected */
var accountRefIndex = 0;

/* Function to make a fetch request with included credentials (cookies)
   https://javascript.info/fetch-crossorigin#credentials
   https://developer.mozilla.org/en-US/docs/Web/API/fetch#credentials
   if we don't have this we get 403 sometimes */
async function fetchWithToken(url, options = {}) {
    const response = await fetch(url, {
        ...options,
        credentials: 'include' // Ensure cookies are included in the request
    });

    /* This is done this way, because we either return json as a response or a string literal.
       You can only read the response body once, so we need to check the content type first and read the
       correct body type. If we try to parse json first and then if it fails read the string, its already empty and will fail.
       https://stackoverflow.com/questions/37121301/how-to-check-if-the-response-of-a-fetch-is-a-json-object-in-javascript */
    let responseBody;
    const contentType = response.headers.get("content-type");
    if (contentType && contentType.indexOf("application/json") !== -1) {
        responseBody = await response.json(); // Attempt to parse the response as JSON
    } else {
        responseBody = await response.text(); // Fallback to text if JSON parsing fails
    }
    if (!response.ok) {
        throw new Error(responseBody);
    }
    return responseBody;
}

async function loadDashboard() {
    try {
        const data = await fetchWithToken('/dashboard/');
        console.log('Fetched data:', data); // Log the fetched data for debugging
        populateDashboard(data);
        customer = data.customer;
        /* set the global customerID variable */
        customerID = data.customer.customerId;
        /* set the global variable for accounts */
        customerAccounts = data.customer.accounts;
        /* populate account dropdowns */
        populateAccountDropdowns();
    } catch (error) {
        console.error('Error loading dashboard:', error);
    }
}

/* function used to refresh the global variables holding customer and customer account information */
async function refreshUserData() {
    const data = await fetchWithToken('/dashboard/');
    customer = data.customer;
    /* Set the global customerID variable */
    customerID = data.customer.customerId;
    /* set the global variable for accounts */
    customerAccounts = data.customer.accounts;
}

function populateDashboard(data) {
    /* populate customer details */
    if (data.customer && data.customer.firstName && data.customer.lastName && data.customer.accounts) {
        document.getElementById('welcomeHeader').innerHTML = `<i style = "font-size:16px">Welcome back, ${data.customer.firstName} ${data.customer.lastName}!</i>`;
        /* generate HTML for accounts table rows */
        var accountsHtml = data.customer.accounts.map(account => `
            <tr><td>${account.accountId}</td>
                <td>${account.accountType}</td>
                <td>${parseFloat(account.balance).toFixed(2)}</td>
            </tr>
        `).join('');
        /* highlight the currently selected account */
        var fromAccountHtml = `<tr><td>${data.customer.accounts.at(accountRefIndex).accountId}</td>`;
        var toAccountHtml = `<tr style="background-color:whitesmoke">
                        <td>${data.customer.accounts.at(accountRefIndex).accountId}</td>`;
        accountsHtml = accountsHtml.replace(fromAccountHtml, toAccountHtml);
        /* insert the generated HTML into the accounts table */
        document.getElementById('account-details').innerHTML = accountsHtml;

        /* populate recent transactions for the selected account */
        populateRecentTransactions(data.customer.accounts[accountRefIndex].transactions);
        /* Process recent and monthly expenditure graphs for selected account */
        processRecentExpenditure(data.customer.accounts[accountRefIndex].transactions);
        processMonthlyExpenditure(data.customer.accounts[accountRefIndex].transactions);
    } else {
        console.error('Customer data is missing or malformed', data);
    }
}

let recentExpenditureChart = null;
function processRecentExpenditure(transactions) {
    /* define the categories for expenditure */
    const categories = ['Dining', 'Entertainment', 'Groceries', 'Healthcare', 'Other', 'Personal', 'Transport', 'Utilities'];
    const categorySums = {};

    /* initialize category sums */
    categories.forEach(category => {
        categorySums[category] = 0;
    });

    /* filter transactions from the last 30 days */
    const now = new Date();
    const last30Days = transactions.filter(transaction => {
        const transactionDate = new Date(transaction.timestamp);
        const diffTime = Math.abs(now - transactionDate);
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        return diffDays <= 30;
    });

    /* sum transactions by category */
    last30Days.forEach(transaction => {
        if (categorySums.hasOwnProperty(transaction.category)) {
            categorySums[transaction.category] += Math.abs(transaction.amount);
        }
    });

    /* prepare data for the chart */
    const labels = Object.keys(categorySums);
    const data = Object.values(categorySums);
    /* destroy the previous chart instance if it exists */
    if (recentExpenditureChart) {
        recentExpenditureChart.destroy();
    }
    /* create a new chart instance */
    const ctx = document.getElementById('recentExpenditureChart').getContext('2d');
    recentExpenditureChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Recent Expenditure',
                data: data,
                backgroundColor: [
                    '#ff6384', '#36a2eb', '#cc65fe', '#ffce56', '#4bc0c0', '#f77825', '#9966ff', '#8e5ea2'
                ]
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    display: false // Hide legend
                }
            },
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}

let monthlyExpenditureChart = null;
function processMonthlyExpenditure(transactions) {
    /* initialize an object to store the sums for each month */
    const monthlySums = {};
    /* iterate over each transaction */
    transactions.forEach(transaction => {
        const date = new Date(transaction.timestamp);
        const yearMonth = `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}`;

        if (!monthlySums[yearMonth]) {
            monthlySums[yearMonth] = 0;
        }
        /* add the transaction amount to the monthly sum */
        monthlySums[yearMonth] += Math.abs(transaction.amount);
    });

    /* prepare data for the chart */
    const labels = Object.keys(monthlySums).sort();
    const data = labels.map(label => monthlySums[label]);
    /* destroy the previous chart instance if it exists */
    if (monthlyExpenditureChart) {
        monthlyExpenditureChart.destroy();
    }
    /* create a new chart instance */
    const ctx = document.getElementById('monthlyExpenditureChart').getContext('2d');
    monthlyExpenditureChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Monthly Expenditure',
                data: data,
                borderColor: '#36a2eb',
                fill: false
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    display: false // Hide legend
                }
            },
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}

/* used for transactions enquiry pagination for better viewing experience */
let transactionsPerPage = 10;
let currentTransactionPage = 1;
let filteredTransactions = [];

function filterTransactions() {
    const selectedAccountId = document.getElementById('accountSelect').value;
    const startDateElement = document.getElementById('startDate');
    const endDateElement = document.getElementById('endDate');
    /* check if one of the dates fields is empty and return error */
    if (!startDateElement.value || !endDateElement.value) {
        showErrorModal('Please select both start and end dates.');
        return;
    }
    /* both dates are present */
    const startDate = new Date(startDateElement.value);
    const endDate = new Date(endDateElement.value);

    /* end date to include the entire day, we need this otherwise we miss most recent transactions. */
    endDate.setHours(23, 59, 59, 999);

    const selectedAccount = customerAccounts.find(account => account.accountId == selectedAccountId);
    /* display error if selected account doesnt exists. */
    if (!selectedAccount) {
        showErrorModal('Selected account not found.');
        return;
    }
    /* filter transactions based on date range */
    filteredTransactions = selectedAccount.transactions.filter(transaction => {
        const transactionDate = new Date(transaction.timestamp);
        return transactionDate >= startDate && transactionDate <= endDate;
    });

    currentTransactionPage = 1;
    paginateTransactions();
}
/* function to handle pagination of transactions */
function paginateTransactions() {
    const start = (currentTransactionPage - 1) * transactionsPerPage;
    const end = start + transactionsPerPage;
    const paginatedTransactions = filteredTransactions.slice(start, end);

    populateTransactionsTable(paginatedTransactions);
    updatePaginationControls();
}
/* function to populate the transactions table */
function populateTransactionsTable(transactions) {
    /* empty transaction table, display information back to this user */
    if (transactions.length === 0) {
        document.getElementById('transactionsTable').innerHTML = `<tr><td colspan="5" class="text-center">--- NO TRANSACTIONS FOR THIS PERIOD ---</td></tr>`;
        return;
    }
    /* generate HTML for each transaction */
    const transactionsHtml = transactions.map(transaction => `
        <tr>
            <td>${transaction.recipientIBAN ? 'Outgoing' : 'Incoming'}</td>
            <td>${transaction.recipientName || transaction.senderName}</td>
            <td style="color: ${transaction.recipientIBAN ? 'red' : 'green'};">${transaction.amount}</td>
            <td>${transaction.category}</td>
            <td>${transaction.timestamp}</td>
        </tr>
    `).join('');
    document.getElementById('transactionsTable').innerHTML = transactionsHtml;
}
/* function to update pagination controls */
function updatePaginationControls() {
    const totalPages = Math.ceil(filteredTransactions.length / transactionsPerPage);
    const pagination = document.getElementById('transactionPagination');
    pagination.innerHTML = '';
    /* create pagination buttons */
    for (let i = 1; i <= totalPages; i++) {
        const pageItem = document.createElement('li');
        pageItem.className = `page-item ${i === currentTransactionPage ? 'active' : ''}`;
        pageItem.innerHTML = `<a class="page-link" href="#" onclick="changeTransactionPage(${i})">${i}</a>`;
        pagination.appendChild(pageItem);
    }
}
/* function to change the current page of transactions */
function changeTransactionPage(page) {
    currentTransactionPage = page;
    paginateTransactions();
}
/* function to populate the recent transactions table */
function populateRecentTransactions(transactions) {
    const recentTransactions = transactions.slice(0, 10); // Get the most recent 10 transactions
    const recentTransactionsHtml = recentTransactions.map(transaction => `
        <tr>
            <td>${transaction.recipientName || transaction.senderName}</td>
            <td style="color: ${transaction.recipientIBAN ? 'red' : 'green'};">${transaction.amount}</td>
            <td>${transaction.timestamp}</td>
        </tr>
    `).join('');
    document.getElementById('recent-transactions').innerHTML = recentTransactionsHtml;
}

/* pop an error modal if something goes wrong */
function showErrorModal(message) {
    const errorModal = new bootstrap.Modal(document.getElementById('errorModal'), {});
    document.getElementById('errorModalBody').innerText = message;
    errorModal.show();
}
/* pop success modal */
function showSuccessMessage(message) {
    const successModal = new bootstrap.Modal(document.getElementById('successModal'), {});
    document.getElementById('successModalBody').innerText = message;
    successModal.show();
}
/* function used to add favourite payee into the database */
async function addFavouritePayee(event) {
    event.preventDefault();
    const name = document.getElementById('payeeName').value;
    const iban = document.getElementById('payeeIban').value;

    try {
        const response = await fetchWithToken(`/api/favourite-payees/add?customerId=${customerID}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ name: name, iban: iban })
        });

        await loadFavouritePayees(); // Refresh the list of favourite payees
    } catch (error) {
        showErrorModal('Failed to add favourite payee! Please try again later');
    }
}
/* function used to remove favourite payee from the database */
async function removeFavouritePayee(payeeId) {
    try {
        const response = await fetchWithToken(`/api/favourite-payees/remove?payeeId=${payeeId}`, {
            method: 'DELETE',
        });
        await loadFavouritePayees(); // Refresh the list of favourite payees
    } catch (error) {
        showErrorModal('Failed to remove favourite payee. Please try again later.')
    }
}

async function loadFavouritePayees() {
    try {
        const data = await fetchWithToken('/api/favourite-payees');
        populateFavouritePayees(data);
    } catch (error) {
        showErrorModal('Failed to remove favourite payee. Please try again later.')
    }
}
/* function used to populate table of favourite payees */
function populateFavouritePayees(data) {
    /* populate favourite payees table */
    const payeesHtml = data.map(payee => `
        <tr>
            <td>${payee.name}</td>
            <td>${payee.iban}</td>
            <td class="text-end text-center">
                <button class="btn btn-danger" onclick="removeFavouritePayee(${payee.id})">Remove</button>
                <button class="btn btn-primary" onclick="transferToPayee('${payee.name}', '${payee.iban}')">Transfer</button>
            </td>
        </tr>
    `).join('');
    document.getElementById('favoritePayeesList').innerHTML = payeesHtml;
}
/* function used to populate the details of selected favourite payee in the transfer fund tab */
function transferToPayee(name, iban) {
    /* move user to Transfer Funds tab */
    var transferFundsTab = new bootstrap.Tab(document.querySelector('a[href="#transfer-funds"]'));
    transferFundsTab.show();

    /* Fill in the recipient details that user selected */
    document.getElementById('recipientName').value = name;
    document.getElementById('recipientIBAN').value = iban;
}
/* function used to load the user cards */
async function loadCards() {
    try {
        const cards = await fetchWithToken('/api/cards', {
            method: 'GET'
        });
        const cardList = document.getElementById('cardList');
        cardList.innerHTML = '';
        cards.forEach(card => {
            let buttonsHtml;
            /* if card is stolen disable the buttons */
            if (card.status === 'Stolen') {
                buttonsHtml = `
                            <button class="btn btn-secondary" disabled>Freeze card</button>
                            <button class="btn btn-secondary" disabled>Stolen</button>`;
            } else {
                /* if card is frozen, give ability to unfreeze it */
                const freezeButton = card.status === 'Frozen' ?
                    `<button class="btn btn-success" onclick="unfreezeCard(${card.cardId})">Unfreeze Card</button>` :
                    `<button class="btn btn-warning" onclick="freezeCard(${card.cardId})">Freeze Card</button>`;
                buttonsHtml = `
                            ${freezeButton}
                            <button class="btn btn-danger" onclick="reportStolen(${card.cardId})">Report Stolen</button>`;
            }
            /* populate the cards*/
            cardList.innerHTML += `
                        <div class="card mb-2">
                            <div class="card-body">
                                <p class="card-text"><strong>Card Number:</strong> ${card.cardNumber}</p>
                                <p class="card-text"><strong>Expiry Date:</strong> ${card.expiryDate}</p>
                                <p class="card-text"><strong>Status:</strong> ${card.status}</p>
                                ${buttonsHtml}
                            </div>
                        </div>
                        `;
        });
    } catch (error) {
        showErrorModal('Failed to load cards {}', error);
    }
}
/* function used to order a new card */
async function orderNewCard() {
    try {
        const response = await fetchWithToken('/api/cards/order', {
            method: 'POST'
        });
        if (response.includes('successful')) {
            showSuccessMessage(response)
            await loadCards();
            await loadDashboard(); /* Update account balance and transactions on the UI. The order is charged 9.99 */
        } else {
            showErrorModal(response);
        }
    } catch (error) {
        showErrorModal('Ordering new card failed. Please try again later ');
    }
}
/* show confirmation modal for ordering card */
function showOrderCardConfirmation() {
    const orderCardModal = new bootstrap.Modal(document.getElementById('orderCardModal'), {});
    orderCardModal.show();
}
/* functionality used to freeze the card */
async function freezeCard(cardId) {
    try {
        await fetchWithToken(`/api/cards/freeze?id=${cardId}`, {
            method: 'PUT'
        });
        loadCards();
    } catch (error) {
        showErrorModal('Failed to freeze card. Please try again later.');
    }
}
/* functionality used to unfreeze card */
async function unfreezeCard(cardId) {
    try {
        await fetchWithToken(`/api/cards/unfreeze?id=${cardId}`, {
            method: 'PUT'
        });
        loadCards();
    } catch (error) {
        showErrorModal('Failed to unfreeze card. Please try again later.');
    }
}
/* functionality used to report stolen card */
async function reportStolen(cardId) {
    try {
        await fetchWithToken(`/api/cards/report-stolen?id=${cardId}`, {
            method: 'PUT'
        });
        alert('Card reported stolen successfully');
        loadCards();
    } catch (error) {
        showErrorModal('Failed to report stolen. Please try again later.');
    }
}

/* Plot all the transactions against the date. CURRENTLY DISABLED */
function createTransactionGraph(data) {
    console.log('Inside create TransactionGraph');

    var amountData = data.customer.accounts.at(accountRefIndex).transactions.map(transaction => transaction.amount);
    console.log('Amount Data', amountData);

    var timestampData = data.customer.accounts.at(accountRefIndex).transactions.map(transaction => transaction.timestamp);
    console.log('Timestamp Data', timestampData);

    var plotData = [];

    for(var i=0;i<timestampData.length;i++)
    {
        x = new Date(timestampData[i].slice(0,10));
        y = amountData[i];
        var json = {x: x, y: y};
        plotData.push(json);
    }
    console.log('Data for plotting', plotData);

    let canvas = document.getElementById("transactionsChart")

    let options = {
        legend: {display: false},
        scales: {
            x: {
                type: 'time',
                display: true,
                title: {
                    display: true,
                    text: 'Date'
                },
                time: {
                    unit: 'day',
                    displayFormats: {
                        'day': 'MMM dd'
                    }
                }
            }  // end scales
        }
    };

    new Chart(canvas, {
            type: "scatter",
            data: {
                datasets: [{
                    pointRadius: 4,
                    pointBackgroundColor: "rgba(0,0,255,1)",
                    data: plotData,
                    label: 'Transactions'
                }]
            },
            options: options
        }
    );
}
/* Create a graph grouping the transactions by month. CURRENTLY DISABLED  */
function createTransactionsByMonthGraph(data) {
    console.log('Inside create TransactionsByMonthGraph');

    var amountData = data.customer.accounts.at(accountRefIndex).transactions.map(transaction => transaction.amount);
    console.log('Amount Data', amountData);

    var timestampData = data.customer.accounts.at(accountRefIndex).transactions.map(transaction => transaction.timestamp);
    console.log('Timestamp Data', timestampData);

    var plotData = [];
    var runningMonthTotal = 0;
    var prevMonth = 0;
    var currMonth = 0;

    for(var i=0;i<timestampData.length;i++)
    {
        m = new Date(timestampData[i].slice(0,10));
        currMonth = m.getMonth();

        if ((currMonth === prevMonth) || (prevMonth === 0))
        {
            runningMonthTotal = runningMonthTotal + amountData[i];
        }
        //Populate the array for the previous month
        if (((currMonth !== prevMonth) && (prevMonth !== 0)))
        {
            var monthNum = prevMonth+1;
            var month = monthNum.toString();
            month = month.padStart(2,"0");
            var maindate = m.getFullYear() + '-' + month + '-' + '01' + 'T' + '00' + ':' + '00' + ':' + '00' + '.' + '000Z';
            mo = Date.parse(maindate);
            x = mo;
            y = runningMonthTotal;
            var json = {x: x, y: y};
            plotData.push(json);
            //Start a new month
            runningMonthTotal = amountData[i];
        }
        //Populate the array for the current
        if (i===(timestampData.length-1))
        {
            var monthNum = currMonth+1;
            var month = monthNum.toString();
            month = month.padStart(2,"0");
            var maindate = m.getFullYear() + '-' + month + '-' + '01' + 'T' + '00' + ':' + '00' + ':' + '00' + '.' + '000Z';
            mo = Date.parse(maindate);
            x = mo;
            y = runningMonthTotal;
            var json = {x: x, y: y};
            plotData.push(json);
            runningMonthTotal = amountData[i];
        }
        prevMonth = currMonth;
    }
    console.log('Data for plotting', plotData);

    let canvas = document.getElementById("transactionsByMonthChart")

    let options = {
        legend: {display: false},
        scales: {
            x: {
                type: 'time',
                display: true,
                title: {
                    display: true,
                    text: 'Month'
                } ,
                time: {
                    minUnit: 'month',
                    displayFormats: {
                        month: 'MMM-yyyy'
                    }
                }

            }  // end scales
        }
    };

    new Chart(canvas, {
            type: "scatter",
            data: {
                datasets: [{
                    pointRadius: 4,
                    pointBackgroundColor: "rgba(0,0,255,1)",
                    data: plotData,
                    label: 'Transactions Total'
                }]
            },
            options: options
        }
    );

}
/* functionality used to show the modal for bill payments */
function showBillPaymentModal(provider) {
    const billPaymentModal = new bootstrap.Modal(document.getElementById('billPaymentModal'));
    document.getElementById('billProvider').innerText = provider;
    billPaymentModal.show();
}
/* functionality used to populate all dropdowns with accounts on the dashboard */
/* the account can be filtered by the account type */
function populateAccountDropdown(selectElementId, accountTypeFilter = null) {
    const selectElement = document.getElementById(selectElementId);
    if (!selectElement) {
        console.error(`Element with id ${selectElementId} not found.`);
        return;
    }

    selectElement.innerHTML = '';
    /* find all accounts or all accounts based on filter */
    customerAccounts.forEach((account, index) => {
        if (!accountTypeFilter || account.accountType === accountTypeFilter) {
            const option = document.createElement('option');
            option.value = account.accountId;
            option.text = `Account ${index + 1} - ${account.iban}`;
            selectElement.appendChild(option);
        }
    });
}

/* Function to populate the account dropdowns for deposit, withdrawal, transfer funds and transaction enquiry */
function populateAccountDropdowns() {
    populateAccountDropdown('transactionAccount', 'Current');
    populateAccountDropdown('billAccountSelect', 'Current');
    populateAccountDropdown('transferAccountSelect', 'Current');
    populateAccountDropdown('accountSelect');  // For filtering in transaction enquiry tab
    populateSavingsAccountDropdown();
}

/* function used to refresh the savingsAccounts dropdown and balance after closing one of the positions */
function populateSavingsAccountDropdown() {
    populateAccountDropdown('savingsAccountSelect', 'Savings');
    updateSavingsAccountInfo()
}
/* function used to pay a bill */
async function payBill() {
    const provider = document.getElementById('billProvider').innerText;
    const amount = document.getElementById('billAmount').value;
    const senderAccountId = document.getElementById('billAccountSelect').value;
    const selectedAccount = customerAccounts.find(account => account.accountId == senderAccountId);

    if (!selectedAccount) {
        showErrorModal('Selected account not found.');
        return;
    }
    /* hard coded recipient ibans for the bill providers */
    const recipientIBAN = {
        'Eir': 'IE29AIBK93115212345678',
        'Vodafone': 'IE29BOFI90135312345678',
        'ElectricIreland': 'IE29BOFI90001712345678'
    };
    /* build transaction object */
    const transaction = {
        accountId: senderAccountId,
        recipientName: provider,
        recipientIBAN: recipientIBAN[provider],
        amount: parseFloat(amount),
        category: 'Utilities',
        timestamp: new Date().toISOString()
    };

    try {
        await fetchWithToken('/transaction/makeTransaction/', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(transaction)
        });
        const billPaymentModal = bootstrap.Modal.getInstance(document.getElementById('billPaymentModal'));
        billPaymentModal.hide();
    } catch (error) {
        showErrorModal('Bill Payment failed');
    }
}
/* function used to make a transaction */
async function makeTransaction(event){
    /* prevent default submission behaviour */
    event.preventDefault();
    /* Get the account ID and other transaction details from the form fields */
    const accountId = document.getElementById('transferAccountSelect').value;
    const amount = document.getElementById('amount').value;
    const category = document.getElementById('category').value;
    const recipientName = document.getElementById('recipientName').value;
    const recipientIBAN = document.getElementById('recipientIBAN').value;

    /* Validate amount */
    if (isNaN(amount) || amount <= 0 || amount > 999999) {
        showErrorModal('Please enter a valid amount between 1 and 999999.');
        return;
    }

    /* Make a POST request to the /api/authenticate endpoint */
    const response = await fetch('/transaction/makeTransaction/', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        /* Convert the details to a JSON string and include it in the request body */
        body: JSON.stringify({ accountId: accountId, amount: amount, recipientName: recipientName, recipientIBAN: recipientIBAN, category: category })
    });

    const resposneBody = await response.text();
    if (response.ok) {
        showSuccessMessage('Transaction successful');

    } else {
        showErrorModal('Transaction failed: ' + resposneBody);
    }
    /* reset the form for another transaction */
    document.getElementById('makeTransactionForm').reset();
}
/* function used to create a new account */
async function makeAccount(event){
    /* prevent default submission behaviour */
    event.preventDefault();
    /* Get the customer ID and password from the form fields */
    const accountType = document.getElementById('accountType').value;

    /* Make a POST request to the /api/authenticate endpoint */
    const response = await fetch('/dashboard/makeAccount/', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        /* Convert the username and password to a JSON string and include it in the request body */
        body: JSON.stringify({ accountType: accountType })
    });
    /* If the response is OK, redirect to the dashboard page */
    if (response.ok) {
        showSuccessMessage("Account created successfully.");
        /* Refresh the dashboard to show the new account */
        loadDashboard();
        /* Redirect to home tab */
        window.location.hash = '#home';
        var homeTab = new bootstrap.Tab(document.querySelector('a[href="#home"]'));
        homeTab.show();
    } else {
        showErrorModal('Account creation failed! Please try again later ');
    }
}
/* function used to populate user profile */
async function showUserProfile() {
    try {
        document.getElementById('profileName').innerText = customer.firstName + ' ' + customer.lastName
        document.getElementById('profileEmail').innerText = customer.email
        document.getElementById('profilePhone').innerText = customer.phoneNumber
        document.getElementById('profileAddress').innerText = customer.addrLine1 + ',' +
                                                                        customer.addrLine2 + ',' +
                                                                        customer.townCity + ',' +
                                                                        customer.countyState;

        const userProfileModal = new bootstrap.Modal(document.getElementById('userProfileModal'));
        userProfileModal.show();
    } catch (error) {
        showErrorModal('Failed to load user profile');
    }
}

async function showContactUs() {
    const contactUsModal = new bootstrap.Modal(document.getElementById('contactUsModal'));
    contactUsModal.show();
}


async function contactUs(event){
    /* prevent default submission behaviour */
    event.preventDefault();
    /* Get the customer ID and password from the form fields */
    const mailText = document.getElementById('mailText').value;

    /* Make a POST request to the /api/authenticate endpoint */
    const response = await fetch('/dashboard/contactUs/', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        /* Convert the username and password to a JSON string and include it in the request body */
        body: JSON.stringify({ mailText: mailText })
    });
    /* If the response is OK, redirect to the dashboard page */
    if (response.ok) {
        showSuccessMessage("Request sent successfully.");
        const currencyConversionModal = bootstrap.Modal.getInstance(document.getElementById('contactUsModal'));
        if (currencyConversionModal) {
            currencyConversionModal.hide();
        }
    } else {
        showErrorModal('Request failed! Please try again later ');
    }
}
/* function used to deposit or withdraw balance from the accounts */
async function handleDepositWithdraw(event) {
    event.preventDefault();
    const amount = document.getElementById('transactionAmount').value;
    const accountId = document.getElementById('transactionAccount').value;
    const transactionType = document.getElementById('transactionType').value;
    /* check if the amount is withing the limits */
    if (isNaN(amount) || amount <= 0 || amount > 999999) {
        showErrorModal('Please enter a valid amount between 1 and 999999.');
        return;
    }

    let url = '';
    /* differentiate between deposit or withdrawal endpoints */
    if (transactionType === 'deposit') {
        url = '/transaction/deposit';
    } else if (transactionType === 'withdraw') {
        url = '/transaction/withdraw';
    } else {
        showErrorModal('Invalid transaction type.');
        return;
    }

    try {
        const response = await fetchWithToken(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ accountId, amount })
        });

        if (response.includes('successful')) {
            if(response.includes('Deposit')) {
                showSuccessMessage('Deposit successful.');
            }else {
                showSuccessMessage('Withdrawal successful.');
            }
            clearDepositWithdrawFields();
        } else {
            showErrorModal('Transaction failed: ' + responseBody);
        }
    } catch (error) {
        showErrorModal("Transaction failed:");
    }
}

/* function used to clear out fields in the deposit/withdrawal tab */
function clearDepositWithdrawFields() {
    document.getElementById('transactionAmount').value = '';
    document.getElementById('transactionAccount').selectedIndex = 0;
    document.getElementById('transactionType').selectedIndex = 0;
}
/* function used to toggle the fields between deposit and withdrawal, depending on the selection of the type */
function toggleDepositWithdrawFields() {
    const transactionType = document.getElementById('transactionType').value;
    const amountLabel = document.querySelector('label[for="transactionAmount"]');
    const submitButton = document.querySelector('#depositWithdrawForm button[type="submit"]');

    if (transactionType === 'deposit') {
        amountLabel.innerText = 'Deposit Amount:';
        submitButton.innerText = 'Deposit';
    } else if (transactionType === 'withdraw') {
        amountLabel.innerText = 'Withdraw Amount:';
        submitButton.innerText = 'Withdraw';
    }
}
/* function used to log out the user */
async function logout() {
    try {
        const response = await fetchWithToken('/api/logout', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.includes('successful')) {
            window.location.href = '/';
        } else {
            showErrorModal('Failed to log out');
        }
    } catch (error) {
        showErrorModal('Failed to log out');
    }
}

/* this runs at the start, used to populate the key datapoints on the dashboard */
window.onload = function() {
    initializeSections(); // Initialize the sections visibility
    loadDashboard();
    loadFavouritePayees();
    loadCards();
    populateAccountDropdowns();

};

/* ensure only the 'home' section is shown by default and other sections are hidden */
function initializeSections() {
    showSection('home'); // Show 'home' section by default
}

/* function used to handle navigation and display of content */
document.querySelectorAll('.menu-link').forEach(link => {
    link.addEventListener('click', event => {
        event.preventDefault();
        const targetId = link.getAttribute('href').substring(1);
        showSection(targetId);
    });
});

function showSection(sectionId) {
    document.querySelectorAll('.tab-pane').forEach(section => {
        section.classList.remove('show', 'active');
    });
    document.getElementById(sectionId).classList.add('show', 'active');
}

/* function used to populate the balance for the selected account. */
function updateSavingsAccountInfo() {
    const selectedAccountId = document.getElementById('savingsAccountSelect').value;
    const account = customerAccounts.find(acc => acc.accountId == selectedAccountId);
    if (account) {
        document.getElementById('savingsAccountBalance').value = parseFloat(account.balance).toFixed(2);
        loadActiveSavings(account.accountId);
    }
}
/* function used to start savings product */
async function startSavings(event) {
    event.preventDefault();
    const amount = document.getElementById('savingsAmount').value;
    const selectedAccountId = document.getElementById('savingsAccountSelect').value;
    /* make sure that user has enough balance */
    if (amount <= 0 || amount > parseFloat(document.getElementById('savingsAccountBalance').value)) {
        showErrorModal('Invalid amount.');
        return;
    }

    const savingsTransaction = {
        accountId: selectedAccountId,
        productName: "No-fixed term savings 5%",
        interestRate: 0.05,
        amount: parseFloat(amount)
    };

    try {
        const response = await fetchWithToken('/api/savings/start', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(savingsTransaction)
        });

        if (response.includes('successful')) {
            showSuccessMessage('Savings started successfully.');
            await refreshUserData();
            await populateSavingsAccountDropdown();
            document.getElementById('savingsAmount').value = '';
        } else {
            showErrorModal('Failed to start savings product ' + response);
        }
    } catch (error) {
        showErrorModal('Failed to start savings product.');
    }
}
/* function used to fetch and populate currently active savings product table */
async function loadActiveSavings(accountId) {
    try {
        const url = `/api/savings/${accountId}`;
        console.log(`Making request to URL: ${url}`); // Debugging log to check the URL
        const response = await fetchWithToken(url);
        const activeSavingsTable = document.getElementById('activeSavingsTable');
        activeSavingsTable.innerHTML = '';
        /* populate currently ACTIVE savings products enabled on the account */
        const activeSavingsFiltered = response.filter(saving => saving.status === 'ACTIVE');

        if (activeSavingsFiltered.length > 0) {
            activeSavingsFiltered.forEach(saving => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${saving.amount.toFixed(2)}</td>
                    <td>${saving.interestRate.toFixed(2)}%</td>
                    <td>${new Date(saving.startDate).toLocaleString()}</td>
                    <td><button class="btn btn-danger" onclick="stopSavings(${saving.transactionId})">Stop</button></td>
                `;
                activeSavingsTable.appendChild(row);
            });
            document.getElementById('activeSavings').style.display = 'block';
        } else {
            document.getElementById('activeSavings').style.display = 'none';
        }
    } catch (error) {
        showErrorModal('Failed to load active savings.');
    }
}
/* function used to stop savings product */
async function stopSavings(transactionId) {
    /* prompt user if he is sure if he wants to stop this savings product */
    if (!confirm('Are you sure you want to stop this savings product?')) {
        return;
    }
    try {
        const response = await fetchWithToken(`/api/savings/stop/${transactionId}`, {
            method: 'POST'
        });

        if (response.includes('successful')) {
            showSuccessMessage('Savings stopped successfully.');
            await refreshUserData();
            await populateSavingsAccountDropdown();
        } else {
            showErrorModal('Failed to stop savings: ' + response);
        }
    } catch (error) {
        showErrorModal('Failed to stop savings.');
    }
}

/* Open transfer modal used for transfer of balance between accounts */
function openTransferModal() {
    populateTransferFromDropdown();
    const transferModal = new bootstrap.Modal(document.getElementById('transferBalanceModal'));
    transferModal.show();
}

/* populate "transfer from" dropdown */
function populateTransferFromDropdown() {
    const transferFromAccountSelect = document.getElementById('transferFromAccount');
    transferFromAccountSelect.innerHTML = '';

    /* Populate current/savings accounts */
    customerAccounts.forEach(account => {
        if (account.accountType === 'Current' || account.accountType === 'Savings') {
            const option = document.createElement('option');
            option.value = account.accountId;
            option.text = `Account ${account.accountId} - ${account.iban}`;
            transferFromAccountSelect.appendChild(option);
        }
    });
    /* Update target accounts dropdown based on the selected source account (we cant have the same target and source */
    updateTargetAccounts();
}

/* update target accounts dropdown. This is often called, bacause the source account needs to be ommited from target
* dropdown */
function updateTargetAccounts() {
    const transferFromAccountSelect = document.getElementById('transferFromAccount');
    const transferToAccountSelect = document.getElementById('transferToAccount');

    const selectedFromAccountId = transferFromAccountSelect.value;
    transferToAccountSelect.innerHTML = '';
    /* filtering out selected from account and only current and savings accounts are allowed */
    customerAccounts.forEach(account => {
        if (account.accountId !== parseInt(selectedFromAccountId) && (account.accountType === 'Current' || account.accountType === 'Savings')) {
            const option = document.createElement('option');
            option.value = account.accountId;
            option.text = `Account ${account.accountId} - ${account.iban}`;
            transferToAccountSelect.appendChild(option);
        }
    });
}

/* function used to transfer balance between accounts */
async function handleTransferBalance(event) {
    event.preventDefault();

    const fromAccountId = document.getElementById('transferFromAccount').value;
    const toAccountId = document.getElementById('transferToAccount').value;
    const amount = parseFloat(document.getElementById('transferAmount').value);


    const toAccount = customerAccounts.find(account => account.accountId == toAccountId);

    const payload = {
        accountId: fromAccountId,
        amount: amount,
        recipientIBAN: toAccount.iban,
        category: "Balance Transfer",
        recipientName: `${customer.firstName} ${customer.lastName}`
    };

    try {
        const response = await fetchWithToken('/transaction/makeTransaction/', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (response.includes("success")) {
            showSuccessMessage('Transfer successful.');
            const transferModal = bootstrap.Modal.getInstance(document.getElementById('transferBalanceModal'));
            transferModal.hide();
            loadDashboard();
        } else {
            showErrorModal('Transfer failed. {}' + response);
        }
    } catch (error) {
        showErrorModal('Transfer failed.');
    }
}

/* Reverse the selected accounts. This is for transfer modal reversal  of target and source button */
function reverseAccounts() {
    const transferFromAccountSelect = document.getElementById('transferFromAccount');
    const transferToAccountSelect = document.getElementById('transferToAccount');

    /* Get current selected values */
    const fromAccountId = transferFromAccountSelect.value;
    const toAccountId = transferToAccountSelect.value;

    /* Swap the values */
    transferFromAccountSelect.value = toAccountId;
    transferToAccountSelect.value = fromAccountId;

    /* Update the target accounts dropdown */
    updateTargetAccounts();
}
