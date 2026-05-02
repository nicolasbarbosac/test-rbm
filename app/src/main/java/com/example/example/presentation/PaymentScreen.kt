package com.example.example.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.example.data.local.TransactionType
import com.example.example.domain.model.AuthorizationResponse
import com.example.example.domain.model.Transaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(viewModel: PaymentViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PaymentScreenContent(
        state = state,
        onAmountChanged = viewModel::onAmountChanged,
        onAuthorize = viewModel::authorize,
        onToggleHistory = viewModel::toggleHistory,
        onSelectTransaction = viewModel::selectTransaction,
        onVoidTransaction = viewModel::voidTransaction,
        onDismissDialog = viewModel::dismissDialog
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreenContent(
    state: PaymentUiState,
    onAmountChanged: (String) -> Unit = {},
    onAuthorize: () -> Unit = {},
    onToggleHistory: () -> Unit = {},
    onSelectTransaction: (Transaction) -> Unit = {},
    onVoidTransaction: () -> Unit = {},
    onDismissDialog: () -> Unit = {}
) {
    val isLoading = state.authStatus is AuthorizationStatus.Loading

    Scaffold(
        topBar = { TopAppBar(title = { Text("Autorización de Pago") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                AuthorizationForm(
                    amount = state.amount,
                    isLoading = isLoading,
                    onAmountChanged = onAmountChanged,
                    onAuthorize = onAuthorize
                )
            }

            item {
                AuthorizationStatusCard(state.authStatus)
            }

            if (state.transactions.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onToggleHistory,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Histórico (${state.transactions.size})")
                    }
                }

                if (state.showHistory) {
                    items(state.transactions) { tx ->
                        TransactionCard(
                            transaction = tx,
                            maskedHexData = tx.hexData,
                            onClick = { onSelectTransaction(tx) }
                        )
                    }
                }
            }
        }
    }

    state.selectedTransaction?.let { tx ->
        VoidConfirmationDialog(
            transaction = tx,
            isVoiding = state.isVoiding,
            onConfirm = onVoidTransaction,
            onDismiss = onDismissDialog
        )
    }
}

@Composable
fun AuthorizationForm(
    amount: String,
    isLoading: Boolean,
    onAmountChanged: (String) -> Unit,
    onAuthorize: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = amount,
            onValueChange = { onAmountChanged(it.replace(Regex("[^0-9]"), "")) },
            label = { Text("Monto") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("amount_field"),
            singleLine = true
        )
        Button(
            onClick = onAuthorize,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("authorize_button"),
            enabled = !isLoading && amount.isNotBlank()
        ) {
            Text(if (isLoading) "Procesando..." else "Autorizar")
        }
    }
}

@Composable
fun AuthorizationStatusCard(status: AuthorizationStatus) {
    when (status) {
        is AuthorizationStatus.Idle -> {}

        is AuthorizationStatus.Loading -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.testTag("loading_indicator"))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Procesando autorización...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        is AuthorizationStatus.Success -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("response_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Aprobada", style = MaterialTheme.typography.titleSmall, color = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow("Receipt", status.response.receiptId)
                    InfoRow("Estado", "${status.response.statusCode} - ${status.response.statusDescription}")
                    InfoRow("HexData", status.response.hexData)
                }
            }
        }

        is AuthorizationStatus.Rejected -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rejected_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Rechazada", style = MaterialTheme.typography.titleSmall, color = Color.Red)
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow("Receipt", status.response.receiptId)
                    InfoRow("Estado", "${status.response.statusCode} - ${status.response.statusDescription}")
                    InfoRow("HexData", status.response.hexData)
                }
            }
        }

        is AuthorizationStatus.NetworkError -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("error_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Error de red", style = MaterialTheme.typography.titleSmall, color = Color(0xFFE65100))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(status.message, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun VoidConfirmationDialog(
    transaction: Transaction,
    isVoiding: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Anular transacción") },
        text = {
            Column {
                InfoRow("Receipt", transaction.receiptId)
                InfoRow("Monto", "$ ${transaction.amount}")
                InfoRow("Estado", transaction.transactionStatus)
                InfoRow("Fecha venta", formatTimestamp(transaction.timestamp))
                transaction.annulmentTimestamp?.let {
                    InfoRow("Fecha anulación", formatTimestamp(it))
                }
                transaction.annulmentStatusCode?.let { code ->
                    InfoRow("Código anulación", "$code - ${transaction.annulmentStatusDescription.orEmpty()}")
                }
            }
        },
        confirmButton = {
            val canVoid = !isVoiding
                    && transaction.statusCode == "00"
                    && transaction.transactionStatus != TransactionType.ANULACION.description
            Button(
                onClick = onConfirm,
                enabled = canVoid
            ) {
                Text(
                    when {
                        isVoiding -> "Anulando..."
                        transaction.statusCode != "00" -> "No anulable"
                        transaction.transactionStatus == TransactionType.ANULACION.description -> "Ya anulada"
                        else -> "Anular"
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun ResponseCard(receiptId: String, status: String, maskedHexData: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("response_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Respuesta", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow("Receipt", receiptId)
            InfoRow("Estado", status)
            InfoRow("HexData", maskedHexData)
        }
    }
}

@Composable
fun TransactionCard(transaction: Transaction, maskedHexData: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "$ ${transaction.amount}", style = MaterialTheme.typography.titleSmall)
                Text(
                    text = transaction.transactionStatus,
                    color = if (transaction.transactionStatus == TransactionType.VENTA.description) Color(0xFF2E7D32) else Color.Red,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            InfoRow("Receipt", transaction.receiptId)
            InfoRow("HexData", maskedHexData)
            InfoRow("Fecha", formatTimestamp(transaction.timestamp))
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodySmall)
    }
}

private fun formatTimestamp(timestamp: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(timestamp))

// region Previews

private val sampleResponse = AuthorizationResponse(
    receiptId = "8f72-4b0b-886b",
    statusCode = "00",
    statusDescription = "Aprobada",
    hexData = "3132********2031"
)

private val sampleRejectedResponse = AuthorizationResponse(
    receiptId = "8f72-4b0b-886b",
    statusCode = "51",
    statusDescription = "Fondos insuficientes",
    hexData = "3132********2031"
)

private val sampleTransaction = Transaction(
    id = 1,
    receiptId = "8f72-4b0b-886b",
    statusCode = "00",
    statusDescription = "Aprobada",
    hexData = "3132********2031",
    amount = "999929",
    timestamp = System.currentTimeMillis(),
    transactionStatus = TransactionType.VENTA.description
)

@Preview(showBackground = true, name = "AuthorizationForm - Empty")
@Composable
private fun PreviewAuthorizationFormEmpty() {
    MaterialTheme {
        AuthorizationForm(
            amount = "",
            isLoading = false,
            onAmountChanged = {},
            onAuthorize = {}
        )
    }
}

@Preview(showBackground = true, name = "AuthorizationForm - With Amount")
@Composable
private fun PreviewAuthorizationFormWithAmount() {
    MaterialTheme {
        AuthorizationForm(
            amount = "999929",
            isLoading = false,
            onAmountChanged = {},
            onAuthorize = {}
        )
    }
}

@Preview(showBackground = true, name = "AuthorizationForm - Loading")
@Composable
private fun PreviewAuthorizationFormLoading() {
    MaterialTheme {
        AuthorizationForm(
            amount = "999929",
            isLoading = true,
            onAmountChanged = {},
            onAuthorize = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading")
@Composable
private fun PreviewLoading() {
    MaterialTheme {
        PaymentScreenContent(
            state = PaymentUiState(
                amount = "999929",
                authStatus = AuthorizationStatus.Loading
            )
        )
    }
}

@Preview(showBackground = true, name = "Success")
@Composable
private fun PreviewSuccess() {
    MaterialTheme {
        PaymentScreenContent(
            state = PaymentUiState(
                amount = "999929",
                authStatus = AuthorizationStatus.Success(sampleResponse)
            )
        )
    }
}

@Preview(showBackground = true, name = "Rejected")
@Composable
private fun PreviewRejected() {
    MaterialTheme {
        PaymentScreenContent(
            state = PaymentUiState(
                amount = "999929",
                authStatus = AuthorizationStatus.Rejected(sampleRejectedResponse)
            )
        )
    }
}

@Preview(showBackground = true, name = "NetworkError")
@Composable
private fun PreviewNetworkError() {
    MaterialTheme {
        PaymentScreenContent(
            state = PaymentUiState(
                amount = "999929",
                authStatus = AuthorizationStatus.NetworkError("Error de red")
            )
        )
    }
}

@Preview(showBackground = true, name = "With History")
@Composable
private fun PreviewWithHistory() {
    MaterialTheme {
        PaymentScreenContent(
            state = PaymentUiState(
                amount = "999929",
                authStatus = AuthorizationStatus.Success(sampleResponse),
                transactions = listOf(sampleTransaction),
                showHistory = true
            )
        )
    }
}

// endregion
