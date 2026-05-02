package com.example.example.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.example.domain.model.Transaction
import com.example.example.domain.usecase.AuthorizePaymentUseCase
import com.example.example.domain.usecase.GetTransactionsUseCase
import com.example.example.domain.usecase.VoidTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val authorizePaymentUseCase: AuthorizePaymentUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val voidTransactionUseCase: VoidTransactionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    init {
        observeTransactions()
    }

    private fun observeTransactions() {
        getTransactionsUseCase()
            .onEach { list -> _uiState.update { it.copy(transactions = list) } }
            .launchIn(viewModelScope)
    }

    fun onAmountChanged(amount: String) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun authorize() {
        val amount = _uiState.value.amount
        if (amount.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(authStatus = AuthorizationStatus.Loading) }
            authorizePaymentUseCase(amount)
                .onSuccess { response ->
                    val status = if (response.statusCode == "00") {
                        AuthorizationStatus.Success(response)
                    } else {
                        AuthorizationStatus.Rejected(response)
                    }
                    _uiState.update { it.copy(authStatus = status) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(authStatus = AuthorizationStatus.NetworkError(e.message ?: "Error desconocido"))
                    }
                }
        }
    }

    fun toggleHistory() {
        _uiState.update { it.copy(showHistory = !it.showHistory) }
    }

    fun selectTransaction(transaction: Transaction) {
        _uiState.update { it.copy(selectedTransaction = transaction) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(selectedTransaction = null) }
    }

    fun voidTransaction() {
        val tx = _uiState.value.selectedTransaction ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isVoiding = true) }
            voidTransactionUseCase(tx.id, tx.receiptId)
                .onSuccess {
                    _uiState.update { it.copy(isVoiding = false, selectedTransaction = null) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isVoiding = false, selectedTransaction = null) }
                }
        }
    }
}
