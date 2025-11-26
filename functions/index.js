const functions = require("firebase-functions/v1");
const admin = require("firebase-admin");

if (!admin.apps.length) {
  admin.initializeApp();
}

// LOG INICIAL
console.log("✅ Functions index cargado");

exports.onFriendRequestCreated = functions.database
  .ref("/friendRequests/{toUid}/{fromUid}")
  .onCreate(async (snapshot, context) => {
    const toUid = context.params.toUid;
    const fromUid = context.params.fromUid;
    const request = snapshot.val();

    console.log(
      "[onFriendRequestCreated] disparada",
      "toUid:", toUid,
      "fromUid:", fromUid,
      "request:", JSON.stringify(request)
    );

    if (!request) {
      console.log("[onFriendRequestCreated] request vacío, no se envía nada");
      return null;
    }

    if (request.status !== "pending") {
      console.log(
        "[onFriendRequestCreated] status no es 'pending' sino:",
        request.status
      );
      return null;
    }

    const toTokenSnap = await admin
      .database()
      .ref(`/users/${toUid}/fcmToken`)
      .once("value");
    const toToken = toTokenSnap.val();

    console.log("[onFriendRequestCreated] token destino:", toToken);

    if (!toToken) {
      console.log("[onFriendRequestCreated] usuario destino sin fcmToken");
      return null;
    }

    const fromNameSnap = await admin
      .database()
      .ref(`/users/${fromUid}/name`)
      .once("value");
    const fromName = fromNameSnap.val() || "Un jugador";

    console.log("[onFriendRequestCreated] nombre origen:", fromName);

    // *******************************
    // *** SOLO DATA PAYLOAD *********
    // *******************************
    const payload = {
      data: {
        type: "friend_request",
        fromUid: fromUid,
        title: "Solicitud de amistad",
        body: `¡Tienes una nueva solicitud de amistad de ${fromName}!`,
      },
    };

    console.log(
      "[onFriendRequestCreated] payload a enviar (DATA ONLY):",
      JSON.stringify(payload)
    );

    return admin
      .messaging()
      .sendToDevice(toToken, payload)
      .then((response) => {
        console.log(
          "[onFriendRequestCreated] respuesta FCM:",
          JSON.stringify(response)
        );
        return null;
      })
      .catch((error) => {
        console.error(
          "[onFriendRequestCreated] ERROR al enviar notificación:",
          error
        );
        return null;
      });
  });

exports.onFriendRequestStatusChanged = functions.database
  .ref("/friendRequests/{toUid}/{fromUid}/status")
  .onUpdate(async (change, context) => {
    const before = change.before.val();
    const after = change.after.val();
    const toUid = context.params.toUid;
    const fromUid = context.params.fromUid;

    console.log(
      "[onFriendRequestStatusChanged] disparada",
      "toUid:", toUid,
      "fromUid:", fromUid,
      "before:", before,
      "after:", after
    );

    if (before === after) {
      console.log("[onFriendRequestStatusChanged] status sin cambio, se sale");
      return null;
    }
    if (after !== "accepted" && after !== "rejected") {
      console.log(
        "[onFriendRequestStatusChanged] status no es accepted/rejected:",
        after
      );
      return null;
    }

    const fromTokenSnap = await admin
      .database()
      .ref(`/users/${fromUid}/fcmToken`)
      .once("value");
    const fromToken = fromTokenSnap.val();

    console.log("[onFriendRequestStatusChanged] token origen:", fromToken);

    if (!fromToken) {
      console.log(
        "[onFriendRequestStatusChanged] usuario origen sin fcmToken"
      );
      return null;
    }

    const toNameSnap = await admin
      .database()
      .ref(`/users/${toUid}/name`)
      .once("value");
    const toName = toNameSnap.val() || "El jugador";

    console.log("[onFriendRequestStatusChanged] nombre destino:", toName);

    const aceptada = after === "accepted";
    const titulo = aceptada ? "Solicitud aceptada" : "Solicitud rechazada";
    const verbo = aceptada ? "aceptado" : "rechazado";

    // *******************************
    // *** SOLO DATA PAYLOAD *********
    // *******************************
    const payload = {
      data: {
        type: "friend_request_response",
        toUid: toUid,
        status: after,
        title: titulo,
        body: `${toName} ha ${verbo} tu solicitud de amistad.`,
      },
    };

    console.log(
      "[onFriendRequestStatusChanged] payload a enviar (DATA ONLY):",
      JSON.stringify(payload)
    );

    return admin
      .messaging()
      .sendToDevice(fromToken, payload)
      .then((response) => {
        console.log(
          "[onFriendRequestStatusChanged] respuesta FCM:",
          JSON.stringify(response)
        );
        return null;
      })
      .catch((error) => {
        console.error(
          "[onFriendRequestStatusChanged] ERROR al enviar notificación:",
          error
        );
        return null;
      });
  });