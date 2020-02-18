package org.mozilla.platoform.service.models

import org.mozilla.msrp.platform.push.models.Message

// Firebase Admin SDK provide the same POJOs. But they are only for the SDK.
// I need the POJO to be deserialize using GSon. So I copy the code from the SDK
// and change the mapping.
class PushPayload(val message: Message)