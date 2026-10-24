
import { Component, OnInit } from '@angular/core';
import { ApiService } from 'src/app/services/api.service';
import { TransactionComponent } from '../transaction/transaction.component';
import { EMPTY, catchError } from 'rxjs';

@Component({
  selector: 'app-transaction-history',
  templateUrl: './transaction-history.component.html',
  styleUrls: ['./transaction-history.component.css'],
})
export class TransactionHistoryComponent implements OnInit {
  transactionHistory: any[] = [];
  filteredTransactionHistory: any[] = [];
  userAccountNumber: string | null = null;
  filterCriteria = '';
  loading = false;
  errorMessage = '';

  constructor(private apiService: ApiService) {}

  ngOnInit(): void {
    this.loadTransactionHistory();
  }

  loadTransactionHistory(): void {
    this.userAccountNumber =
      TransactionComponent.getAccountNumberFromToken();

    this.loading = true;
    this.errorMessage = '';

    this.apiService
      .getTransactions()
      .pipe(
        catchError((error) => {
          console.error('Error fetching transaction history:', error);
          this.errorMessage =
            'Unable to load transactions. Please try again.';
          return EMPTY;
        })
      )
      .subscribe({
        next: (data: any[]) => {
          this.transactionHistory = (data || []).slice().sort(
            (a, b) =>
              new Date(b.transactionDate).getTime() -
              new Date(a.transactionDate).getTime()
          );

          this.filterTransactions();
          this.loading = false;
        },
        complete: () => {
          this.loading = false;
        },
      });
  }

  getTransactionStatus(transaction: any): string {
    switch (transaction.transactionType) {
      case 'CASH_DEPOSIT':
        return 'Deposit';

      case 'CASH_WITHDRAWAL':
        return 'Withdrawal';

      case 'CASH_TRANSFER':
        if (
          transaction.targetAccountNumber === this.userAccountNumber
        ) {
          return 'Transfer In';
        }

        return 'Transfer Out';

      case 'CASH_CREDIT':
        return 'Credit';

      default:
        return transaction.transactionType || 'Transaction';
    }
  }

  isIncoming(transaction: any): boolean {
    if (transaction.transactionType === 'CASH_DEPOSIT') {
      return true;
    }

    if (transaction.transactionType === 'CASH_CREDIT') {
      return true;
    }

    if (transaction.transactionType === 'CASH_TRANSFER') {
      return (
        transaction.targetAccountNumber === this.userAccountNumber
      );
    }

    return false;
  }

  filterTransactions(): void {
    let results = [...this.transactionHistory];

    if (this.filterCriteria === 'Deposit') {
      results = results.filter(
        (transaction) =>
          transaction.transactionType === 'CASH_DEPOSIT'
      );
    } else if (this.filterCriteria === 'Withdrawal') {
      results = results.filter(
        (transaction) =>
          transaction.transactionType === 'CASH_WITHDRAWAL'
      );
    } else if (this.filterCriteria === 'Transfer') {
      results = results.filter(
        (transaction) =>
          transaction.transactionType === 'CASH_TRANSFER' ||
          transaction.transactionType === 'CASH_CREDIT'
      );
    }

    this.filteredTransactionHistory = results;
  }

  onFilterCriteriaChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.filterCriteria = select.value;
    this.filterTransactions();
  }
}
