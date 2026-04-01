package com.contractpulse.scheduler;

import com.contractpulse.timeentry.ApprovalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * Testes unitários para o WeeklyApprovalJob.
 * Verifica que os jobs delegam corretamente ao ApprovalService
 * e tratam exceções sem propagar.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WeeklyApprovalJob")
class WeeklyApprovalJobTest {

    @Mock
    private ApprovalService approvalService;

    @InjectMocks
    private WeeklyApprovalJob weeklyApprovalJob;

    @Nested
    @DisplayName("promoteSubmittedEntries")
    class PromoteSubmittedEntries {

        @Test
        @DisplayName("deve delegar promoção ao ApprovalService")
        void shouldDelegateToApprovalService() {
            // Arrange
            when(approvalService.promoteSubmittedToPendingApproval()).thenReturn(5);

            // Act
            weeklyApprovalJob.promoteSubmittedEntries();

            // Assert
            verify(approvalService).promoteSubmittedToPendingApproval();
        }

        @Test
        @DisplayName("deve capturar exceção sem propagar")
        void shouldCatchExceptionWithoutPropagating() {
            // Arrange
            when(approvalService.promoteSubmittedToPendingApproval())
                    .thenThrow(new RuntimeException("DB offline"));

            // Act — não deve lançar exceção
            weeklyApprovalJob.promoteSubmittedEntries();

            // Assert
            verify(approvalService).promoteSubmittedToPendingApproval();
        }
    }

    @Nested
    @DisplayName("autoApproveExpiredEntries")
    class AutoApproveExpiredEntries {

        @Test
        @DisplayName("deve delegar auto-aprovação ao ApprovalService")
        void shouldDelegateAutoApproveToApprovalService() {
            // Arrange
            when(approvalService.autoApproveExpiredEntries(WeeklyApprovalJob.SYSTEM_USER_ID))
                    .thenReturn(3);

            // Act
            weeklyApprovalJob.autoApproveExpiredEntries();

            // Assert
            verify(approvalService).autoApproveExpiredEntries(WeeklyApprovalJob.SYSTEM_USER_ID);
        }

        @Test
        @DisplayName("deve capturar exceção sem propagar")
        void shouldCatchExceptionWithoutPropagating() {
            // Arrange
            when(approvalService.autoApproveExpiredEntries(WeeklyApprovalJob.SYSTEM_USER_ID))
                    .thenThrow(new RuntimeException("DB offline"));

            // Act — não deve lançar exceção
            weeklyApprovalJob.autoApproveExpiredEntries();

            // Assert
            verify(approvalService).autoApproveExpiredEntries(WeeklyApprovalJob.SYSTEM_USER_ID);
        }
    }
}
