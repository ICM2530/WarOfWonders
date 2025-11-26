const functions = require("firebase-functions/v1");
const admin = require("firebase-admin");

if (!admin.apps.length) {
  admin.initializeApp();
}

exports.onUserGameEvent = functions.database
  .ref("/userEvents/{uid}/{eventId}")
  .onCreate(async (snapshot, context) => {
    const uid = context.params.uid;
    const eventData = snapshot.val();
    if (!eventData) return null;

    const tokenSnap = await admin
      .database()
      .ref(`/users/${uid}/fcmToken`)
      .once("value");

    const token = tokenSnap.val();
    if (!token) return null;

    const title = eventData.title || "Notificación";
    const body = eventData.body || "Tienes una actualización en el juego";

    const payload = {
      data: {
        type: "game_event",
        title: title,
        body: body,
      },
    };

    return admin.messaging().sendToDevice(token, payload);
  });

exports.onFriendRequestCreated = functions.database
  .ref("/friendRequests/{toUid}/{fromUid}")
  .onCreate(async (snapshot, context) => {
    const request = snapshot.val();

    if (!request || request.status !== "pending") {
      return null;
    }

    const usersSnap = await admin.database().ref("/users").once("value");

    const tokens = [];
    usersSnap.forEach((child) => {
      const t = child.child("fcmToken").val();
      if (t) {
        tokens.push(t);
      }
    });

    if (!tokens.length) {
      return null;
    }

    const payload = {
      data: {
        type: "friend_request",
        title: "Notificación",
        body: "Tienes una nueva actividad en War of Wonders",
      },
    };

    return admin
      .messaging()
      .sendMulticast({
        tokens,
        data: payload.data,
      })
      .then(() => null)
      .catch(() => null);
  });

exports.onFriendRequestStatusChanged = functions.database
  .ref("/friendRequests/{toUid}/{fromUid}/status")
  .onUpdate(async (change, context) => {
    const before = change.before.val();
    const after = change.after.val();

    if (before === after) {
      return null;
    }

    if (after !== "accepted" && after !== "rejected") {
      return null;
    }

    const usersSnap = await admin.database().ref("/users").once("value");

    const tokens = [];
    usersSnap.forEach((child) => {
      const t = child.child("fcmToken").val();
      if (t) {
        tokens.push(t);
      }
    });

    if (!tokens.length) {
      return null;
    }

    const payload = {
      data: {
        type: "friend_request_response",
        status: after,
        title: "Notificación",
        body: "Tu solicitud de amistad ha cambiado de estado",
      },
    };

    return admin
      .messaging()
      .sendMulticast({
        tokens,
        data: payload.data,
      })
      .then(() => null)
      .catch(() => null);
  });