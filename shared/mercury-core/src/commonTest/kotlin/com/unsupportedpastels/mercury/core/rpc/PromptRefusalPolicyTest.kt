package com.unsupportedpastels.mercury.core.rpc

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PromptRefusalPolicyTest {
    @Test fun ownerBusyProvesNothingWasSentAndAllowsResend() {
        val refusal = PromptRefusalPolicy.classify(4090, "SESSION_OWNER_BUSY", "server prose")!!
        assertEquals(PromptRefusalKind.OwnerBusy, refusal.kind)
        assertTrue(refusal.nothingSent)
        assertTrue(refusal.retryable)
    }

    @Test fun notOwnedProvesNothingWasSentButResendRepeatsTheRefusal() {
        val refusal = PromptRefusalPolicy.classify(4090, "SESSION_NOT_OWNED", null)!!
        assertEquals(PromptRefusalKind.NotOwned, refusal.kind)
        assertTrue(refusal.nothingSent)
        assertFalse(refusal.retryable)
    }

    @Test fun unconfirmedMayAlreadyBeQueuedSoNeverOffersResend() {
        val refusal = PromptRefusalPolicy.classify(4090, "SESSION_OWNER_UNCONFIRMED", null)!!
        assertEquals(PromptRefusalKind.OwnerUnconfirmed, refusal.kind)
        assertFalse(refusal.nothingSent)
        assertFalse(refusal.retryable)
    }

    @Test fun legacyLiveOwnerProseWithoutReasonIsNotOwned() {
        val refusal = PromptRefusalPolicy.classify(4090, null, "Session abc already has a live owner")
        assertEquals(PromptRefusalKind.NotOwned, refusal?.kind)
    }

    @Test fun otherCodesAndUnknownReasonsKeepTheGenericFailure() {
        assertNull(PromptRefusalPolicy.classify(-32000, "SESSION_OWNER_BUSY", null))
        assertNull(PromptRefusalPolicy.classify(null, "SESSION_NOT_OWNED", null))
        assertNull(PromptRefusalPolicy.classify(4090, "SESSION_COORDINATION_UNAVAILABLE", "already has a live owner"))
        assertNull(PromptRefusalPolicy.classify(4090, null, "normal admission reached"))
    }

    @Test fun shownTextNeverEchoesServerProse() {
        val prose = "secret prompt text echoed by a server"
        val refusal = PromptRefusalPolicy.classify(4090, "SESSION_OWNER_BUSY", prose)!!
        assertFalse(refusal.message.contains(prose))
    }
}
