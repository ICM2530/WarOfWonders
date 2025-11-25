const functions = require("firebase-functions/v1");
const admin = require("firebase-admin");

if (!admin.apps.length) {
  admin.initializeApp();
}

exports.onFriendRequestCreated = functions.database
  .ref("/friendRequests/{toUid}/{fromUid}")
  .onCreate(async (snapshot, context) => {
    const toUid = context.params.toUid;
    const fromUid = context.params.fromUid;
    const request = snapshot.val();
    if (!request || request.status !== "pending") return null;

    const userSnap = await admin
      .database()
      .ref(`/users/${toUid}/fcmToken`)
      .once("value");
    const token = userSnap.val();
    if (!token) return null;

    const payload = {
      notification: {
        title: "Nueva solicitud de amistad",
        body: "Tienes una nueva solicitud de amistad en War of Wonders",
      },
      data: {
        type: "friend_request",
        fromUid: fromUid,
      },
    };

    return admin.messaging().sendToDevice(token, payload);
  });

exports.onFriendRequestStatusChanged = functions.database
  .ref("/friendRequests/{toUid}/{fromUid}/status")
  .onUpdate(async (change, context) => {
    const before = change.before.val();
    const after = change.after.val();
    const toUid = context.params.toUid;
    const fromUid = context.params.fromUid;

    if (before === after) return null;
    if (after !== "accepted" && after !== "rejected") return null;

    const userSnap = await admin
      .database()
      .ref(`/users/${fromUid}/fcmToken`)
      .once("value");
    const token = userSnap.val();
    if (!token) return null;

    const body =
      after === "accepted"
        ? "Han aceptado tu solicitud de amistad"
        : "Han rechazado tu solicitud de amistad";

    const payload = {
      notification: {
        title: "Respuesta a tu solicitud",
        body: body,
      },
      data: {
        type: "friend_request_response",
        toUid: toUid,
        status: after,
      },
    };

    return admin.messaging().sendToDevice(token, payload);
  });