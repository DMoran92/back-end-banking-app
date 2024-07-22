

let custSelect = []
let accountList = []
let transactionsList = []

function getCustomer(){
    event.preventDefault();
    /* get customer name by input */
    customerLastNameInput = document.getElementById("searchCustomerName").value;
    let custSearch = customerList.find(c => c.lastName.toUpperCase() === customerLastNameInput.toUpperCase()) //remove case sensitivity

    if(custSearch == null){
        document.getElementById('customerCard').style.display = 'none';
        window.alert("No Customer Found")
    } else{
        /* control visibility of cards */
        document.getElementById('customerCard').style.display = 'block';
        document.getElementById('accountsCard').style.display = 'none';
        document.getElementById('transactionsCard').style.display = 'none';

        /* populate customer table */
        document.getElementById('customerId').innerText = custSearch.customerId
        document.getElementById('customerName').innerText = custSearch.firstName + ' ' + custSearch.lastName
        document.getElementById('customerUsername').innerText = custSearch.username
    }
}

function getAccounts(customerNumber) {
    /* find customer based on selected customerID */
    custSelect = customerList.find(c => c.customerId === customerNumber)
    /* populate accounts list for that customer */
    document.getElementById('account-details').innerHTML = custSelect.accounts.map(account => `
            <tr>
                <td>${account.iban}</td>
                <td>${account.accountType} (${account.currency})</td>
                <td>${parseFloat(account.balance).toFixed(2)}</td>
                <td style="display: none">${account.customerId}</td> <!-- hidden field to pass customerId again -->
            </tr>
        `).join('');
}

let transactionsPerPage = 10; // Number of transactions per page
let currentTransactionPage = 1;
let filteredTransactions = [];
let selectedIban = "null";

function getTransactions(accountIban, accountCustomerNumber) {

    /* Assign selectedIban with the provided accountIban */
    selectedIban = accountIban;
    /* find customer and their accounts based on selected customerID */
    custSelect = customerList.find(c => c.customerId === accountCustomerNumber)
    accountList = custSelect.accounts

    /* find the specific account based on IBAN and get transactions */
    let selectedAccount = accountList.find(a => a.iban === accountIban)
    transactionsList = selectedAccount.transactions

    /* Sort transactions by timestamp in descending order */
    transactionsList.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));

    /* paginate transactions */
    filteredTransactions = transactionsList;
    currentTransactionPage = 1;
    paginateTransactions();
}

/* Function to paginate transactions */
function paginateTransactions() {
    const start = (currentTransactionPage - 1) * transactionsPerPage;
    const end = start + transactionsPerPage;
    const paginatedTransactions = filteredTransactions.slice(start, end);

    populateTransactionsTable(paginatedTransactions);
    updatePaginationControls();
}

/* Function to populate transactions table */
function populateTransactionsTable(transactions) {
    if (transactions.length === 0) {
        document.getElementById('transaction-details').innerHTML = `<tr><td colspan="4" class="text-center">--- NO TRANSACTIONS FOR THIS ACCOUNT ---</td></tr>`;
        return;
    }

    const transactionsHtml = transactions.map(transaction => {
        let isOutgoing = !selectedIban.includes(transaction.recipientIBAN);
        let transactionName = isOutgoing ? transaction.recipientName : transaction.senderName;

        if (transaction.category === "Cash Withdrawal") {
            transactionName = "Cash Withdrawal";
            isOutgoing = true;
        } else if (transaction.category === "Cash Deposit") {
            transactionName = "Cash Deposit";
            isOutgoing = false;
        }

        return `
            <tr>
                <td>${transactionName}</td>
                <td style="color: ${isOutgoing ? 'red' : 'green'};">${transaction.amount}</td>
                <td>${transaction.category}</td>
                <td>${transaction.timestamp}</td>
            </tr>
        `;
    }).join('');

    document.getElementById('transaction-details').innerHTML = transactionsHtml;
}

/* Function to update pagination controls */
function updatePaginationControls() {
    const totalPages = Math.ceil(filteredTransactions.length / transactionsPerPage);
    const pagination = document.getElementById('transactionPagination');
    pagination.innerHTML = '';

    for (let i = 1; i <= totalPages; i++) {
        const pageItem = document.createElement('li');
        pageItem.className = `page-item ${i === currentTransactionPage ? 'active' : ''}`;
        pageItem.innerHTML = `<a class="page-link" href="#" onclick="changeTransactionPage(${i})">${i}</a>`;
        pagination.appendChild(pageItem);
    }
}

/* Function to change the current page of transactions */
function changeTransactionPage(page) {
    /* the page will go back to top, prevent default behaviour so the screen stays at the same place */
    event.preventDefault();
    currentTransactionPage = page;
    paginateTransactions();
}
