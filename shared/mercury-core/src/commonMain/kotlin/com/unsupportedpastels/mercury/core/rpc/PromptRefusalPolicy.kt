package com.unsupportedpastels.mercury.core.rpc

/** Why Hermes refused a `prompt.submit` because another owner holds the session. */
enum class PromptRefusalKind { OwnerBusy, NotOwned, OwnerUnconfirmed }

data class PromptRefusal(
    val kind: PromptRefusalKind,
    /** Client-authored text, safe to show; server prose is never echoed. */
    val message: String,
    /** Hermes proved nothing was queued, so the optimistic user row can be withdrawn. */
    val nothingSent: Boolean,
    /** Sending the same text again cannot run a second copy. */
    val retryable: Boolean,
)

/**
 * Classifies Hermes's 4090 ownership refusals of `prompt.submit`.
 *
 * Since the gateway cutover, a send to a chat whose gateway ledger holds a running or
 * queued turn is forwarded to that owner's FIFO, or refused with a machine reason in
 * `error.data.reason`. Only `SESSION_OWNER_UNCONFIRMED` may already be queued.
 * Any other 4090 (unknown reason) returns null and keeps the generic failure.
 */
object PromptRefusalPolicy {
    const val SESSION_REFUSED_CODE: Long = 4090L

    private val ownerBusy = PromptRefusal(
        kind = PromptRefusalKind.OwnerBusy,
        message = "Hermes is running another turn in this chat and could not queue your message. " +
            "Nothing was sent. Send it again when that turn ends.",
        nothingSent = true,
        retryable = true,
    )

    private val notOwned = PromptRefusal(
        kind = PromptRefusalKind.NotOwned,
        message = "Another Hermes client or the gateway controls this chat. " +
            "Nothing was sent. Send it after that work finishes, or start a new chat.",
        nothingSent = true,
        retryable = false,
    )

    private val ownerUnconfirmed = PromptRefusal(
        kind = PromptRefusalKind.OwnerUnconfirmed,
        message = "Hermes may already have queued your message behind the current turn. " +
            "Check the chat before sending it again.",
        nothingSent = false,
        retryable = false,
    )

    // Backends older than the machine reason only carry this prose.
    private val liveOwnerProse = Regex("already has a live owner", RegexOption.IGNORE_CASE)

    fun classify(code: Long?, reason: String?, message: String?): PromptRefusal? {
        if (code != SESSION_REFUSED_CODE) return null
        return when (reason?.trim()) {
            "SESSION_OWNER_BUSY" -> ownerBusy
            "SESSION_NOT_OWNED" -> notOwned
            "SESSION_OWNER_UNCONFIRMED" -> ownerUnconfirmed
            null, "" -> notOwned.takeIf { message != null && liveOwnerProse.containsMatchIn(message) }
            else -> null
        }
    }
}
