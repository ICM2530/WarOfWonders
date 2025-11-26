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

    const toTokenSnap = await admin
      .database()
      .ref(`/users/${toUid}/fcmToken`)
      .once("value");
    const toToken = toTokenSnap.val();
    if (!toToken) return null;

    const fromNameSnap = await admin
      .database()
      .ref(`/users/${fromUid}/name`)
      .once("value");
    const fromName = fromNameSnap.val() || "Un jugador";

    const payload = {
      notification: {
        title: "Solicitud de amistad",
        body: `¡Tienes una nueva solicitud de amistad de ${fromName}!`,
      },
      data: {
        type: "friend_request",
        fromUid: fromUid,
      },
    };

    return admin.messaging().sendToDevice(toToken, payload);
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

    const fromTokenSnap = await admin
      .database()
      .ref(`/users/${fromUid}/fcmToken`)
      .once("value");
    const fromToken = fromTokenSnap.val();
    if (!fromToken) return null;

    const toNameSnap = await admin
      .database()
      .ref(`/users/${toUid}/name`)
      .once("value");
    const toName = toNameSnap.val() || "El jugador";

    const aceptada = after === "accepted";
    const titulo = aceptada ? "Solicitud aceptada" : "Solicitud rechazada";
    const verbo = aceptada ? "aceptado" : "rechazado";

    const payload = {
      notification: {
        title: titulo,
        body: `${toName} ha ${verbo} tu solicitud de amistad.`,
      },
      data: {
        type: "friend_request_response",
        toUid: toUid,
        status: after,
      },
    };

    return admin.messaging().sendToDevice(fromToken, payload);
  });