package com.example.example

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.example.example.data.local.TransactionType
import com.example.example.domain.model.AuthorizationResponse
import com.example.example.domain.model.Transaction
import com.example.example.presentation.AuthorizationStatus
import com.example.example.presentation.PaymentScreenContent
import com.example.example.presentation.PaymentUiState
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class PaymentScreenContentTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun init() {
        hiltRule.inject()
    }

    // region AuthorizationForm

    @Test
    fun amountField_displaysFilteredValue() {
        composeTestRule.setContent {
            MaterialTheme {
                PaymentScreenContent(state = PaymentUiState(amount = "685"))
            }
        }
        composeTestRule.onNodeWithTag("amount_field").assertTextContains("685")
    }

    @Test
    fun authorizeButton_disabledWhenAmountEmpty() {
        composeTestRule.setContent {
            MaterialTheme {
                PaymentScreenContent(state = PaymentUiState(amount = ""))
            }
        }
        composeTestRule.onNodeWithTag("authorize_button").assertIsNotEnabled()
    }

    @Test
    fun authorizeButton_enabledWhenAmountFilled() {
        composeTestRule.setContent {
            MaterialTheme {
                PaymentScreenContent(state = PaymentUiState(amount = "999929"))
            }
        }
        composeTestRule.onNodeWithTag("authorize_button").assertIsEnabled()
    }

    @Test
    fun authorizeButton_disabledAndShowsProcessingWhenLoading() {
        composeTestRule.setContent {
            MaterialTheme {
                PaymentScreenContent(
                    state = PaymentUiState(
                        amount = "999929",
                        authStatus = AuthorizationStatus.Loading
                    )
                )
            }
        }
        composeTestRule.onNodeWithTag("authorize_button").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Procesando...").assertIsDisplayed()
    }

    // endregion

    // region AuthorizationStatusCard

    @Test
    fun statusCard_showsLoadingState() {
        composeTestRule.setContent {
            MaterialTheme {
                PaymentScreenContent(
                    state = PaymentUiState(
                        amount = "999929",
                        authStatus = AuthorizationStatus.Loading
                    )
                )
            }
        }
        composeTestRule.onNodeWithTag("loading_indicator").assertIsDisplayed()
        composeTestRule.onNodeWithText("Procesando autorización...").assertIsDisplayed()
    }

    @Test
    fun statusCard_showsSuccessState() {
        val response = AuthorizationResponse("8f72-4b0b-886b", "00", "Aprobada", "3132********2031")
        composeTestRule.setContent {
            MaterialTheme {
                PaymentScreenContent(
                    state = PaymentUiState(authStatus = AuthorizationStatus.Success(response))
                )
            }
        }
        composeTestRule.onNodeWithTag("response_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Aprobada").assertIsDisplayed()
        composeTestRule.onNodeWithText("8f72-4b0b-886b").assertIsDisplayed()
        composeTestRule.onNodeWithText("00 - Aprobada").assertIsDisplayed()
        composeTestRule.onNodeWithText("3132********2031").assertIsDisplayed()
    }

    @Test
    fun statusCard_showsRejectedState() {
        val response = AuthorizationResponse("8f72-4b0b-886b", "51", "Fondos insuficientes", "3132********2031")
        composeTestRule.setContent {
            MaterialTheme {
                PaymentScreenContent(
                    state = PaymentUiState(authStatus = AuthorizationStatus.Rejected(response))
                )
            }
        }
        composeTestRule.onNodeWithTag("rejected_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rechazada").assertIsDisplayed()
        composeTestRule.onNodeWithText("51 - Fondos insuficientes").assertIsDisplayed()
    }

    @Test
    fun statusCard_showsNetworkErrorState() {
        composeTestRule.setContent {
            MaterialTheme {
                PaymentScreenContent(
                    state = PaymentUiState(authStatus = AuthorizationStatus.NetworkError("Sin conexión"))
                )
            }
        }
        composeTestRule.onNodeWithTag("error_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Error de red").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sin conexión").assertIsDisplayed()
    }

    // endregion

    // region History

    @Test
    fun history_showsButtonWithTransactionCount() {
        composeTestRule.setContent {
            MaterialTheme {
                PaymentScreenContent(
                    state = PaymentUiState(transactions = listOf(sampleTransaction()))
                )
            }
        }
        composeTestRule.onNodeWithText("Histórico (1)").assertIsDisplayed()
    }

    @Test
    fun history_showsTransactionsWhenExpanded() {
        composeTestRule.setContent {
            MaterialTheme {
                PaymentScreenContent(
                    state = PaymentUiState(
                        transactions = listOf(sampleTransaction()),
                        showHistory = true
                    )
                )
            }
        }
        composeTestRule.onNodeWithText("$ 999929").assertIsDisplayed()
        composeTestRule.onNodeWithText(TransactionType.VENTA.description).assertIsDisplayed()
    }

    @Test
    fun history_notVisibleWhenNoTransactions() {
        composeTestRule.setContent {
            MaterialTheme {
                PaymentScreenContent(state = PaymentUiState())
            }
        }
        composeTestRule.onNodeWithText("Histórico", substring = true).assertDoesNotExist()
    }

    // endregion

    // region Idle

    @Test
    fun idleState_showsTitleAndForm() {
        composeTestRule.setContent {
            MaterialTheme {
                PaymentScreenContent(state = PaymentUiState())
            }
        }
        composeTestRule.onNodeWithText("Autorización de Pago").assertIsDisplayed()
        composeTestRule.onNodeWithTag("amount_field").assertIsDisplayed()
        composeTestRule.onNodeWithText("Autorizar").assertIsDisplayed()
    }

    // endregion

    private fun sampleTransaction() = Transaction(
        id = 1,
        receiptId = "8f72-4b0b-886b",
        statusCode = "00",
        statusDescription = "Aprobada",
        hexData = "3132********2031",
        amount = "999929",
        timestamp = 1000L,
        transactionStatus = TransactionType.VENTA.description
    )
}
