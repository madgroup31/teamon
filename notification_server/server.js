const admin = require('firebase-admin');
const express = require('express');
const serviceAccount = require('./config/serviceAccountKey.json');
const { QuerySnapshot } = require('firebase-admin/firestore');

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();
const app = express();
const port = 3001;

let isInitialTasksSnapshotProcessed = false;
let isInitialMessagesSnapshotProcessed = false;

// Listen for Firestore changes
db.collection('history').onSnapshot(snapshot => {
    if (!isInitialTasksSnapshotProcessed) {
        // Skip the initial snapshot
        isInitialTasksSnapshotProcessed = true;
        return;
    }

    snapshot.docChanges().forEach(change => {
        if (change.type === 'added' || change.type === 'modified') {
            let newValue = change.doc.data();
            
            db.collection("tasks")
                .where("history", "array-contains", change.doc.id)
                .get()
                .then(tasksQuerySnapshot => {
                    if (!tasksQuerySnapshot.empty) {
                        let taskDoc = tasksQuerySnapshot.docs[0];
                        let taskId = taskDoc.id;
                        let taskName = taskDoc.data().taskName;

                        db.collection("projects")
                            .where("tasks", "array-contains", taskId)
                            .get()
                            .then(projectsQuerySnapshot => {
                                if (!projectsQuerySnapshot.empty) {
                                    let projectName = projectsQuerySnapshot.docs[0].data().projectName;
                                    let projectId = projectsQuerySnapshot.docs[0].id

                                    const message = {
                                        data: {
                                            taskId: taskId,
                                            projectId: projectId
                                        },
                                        android: {
                                            notification: {
                                            default_sound: true,
                                            default_vibrate_timings: true,
                                            title: taskName +" in "+projectName,
                                            body: newValue.text,
                                                channelId: "history",
                                                icon: 'ic_action_name',
                                                tag: change.doc.data().user,
                                            },
                                            },
                                        topic: taskId.toString()
                                        };
                                    
                
                                    admin.messaging().send(message)
                                        .then(response => {
                                            console.log("Notification sent successfully ("+response+"): ", message);
                                        })
                                        .catch(error => {
                                            console.log('Error sending notification:', error);
                                        });
                                }
                            })
                            .catch(error => {
                                console.error("Error getting projects:", error);
                            });
                    }
                })
                .catch(error => {
                    console.error("Error getting tasks:", error);
                });
        }
    });
});

db.collection('messages').onSnapshot(snapshot => {
    if (!isInitialMessagesSnapshotProcessed) {
        // Skip the initial snapshot
        isInitialMessagesSnapshotProcessed = true;
        return;
    }

    snapshot.docChanges().forEach(change => {
        if (change.type === 'added' || change.type === 'modified') {
            let newValue = change.doc.data();
            let unread = change.doc.data().unread;

            db.collection("chats")
                .doc(change.doc.data().chatId)
                .get()
                .then(chatQuerySnapshot => {
                    if (chatQuerySnapshot.exists) {

                        let chatId = chatQuerySnapshot.id
                        let teamId = chatQuerySnapshot.data().teamId;
                        let personal = chatQuerySnapshot.data().personal;

                        let senderId = change.doc.data().senderId;

                        db.collection("teams")
                            .doc(teamId)
                            .get()
                            .then(teamQuerySnapshot => {
                                if (teamQuerySnapshot.exists) {
                                    let teamName = teamQuerySnapshot.data().name;
                                    let teamImageSource = teamQuerySnapshot.data().imageSource;
                                    let teamImage = teamQuerySnapshot.data().image;

                                    
                                    db.collection("users")
                                    .doc(senderId)
                                    .get()
                                    .then(userQuerySnapshot => {
                                        if (userQuerySnapshot.exists) {
                                            let senderNickname = userQuerySnapshot.data().nickname;
                                            let userImageSource = userQuerySnapshot.data().profileImageSource;
                                            let message; let title; let body; let image;
                                            if(personal) {
                                                title = senderNickname + " | " + teamName;
                                                body = change.doc.data().content;
                                                image = userQuerySnapshot.data().profileImage;
                                            }
                                            else {
                                                title = teamName
                                                body = senderNickname + ": "+ change.doc.data().content
                                            }
                                            if(personal) {
                                                if(userImageSource == "MONOGRAM") {

                                                    message = {
                                                        data: {
                                                            userId: senderId,
                                                            chatId: chatId
                                                        },
                                                        android: {
                                                            notification: {
                                                                default_sound: true,
                                                                default_vibrate_timings: true,
                                                                title: title,
                                                                body: body,
                                                                channelId: "messages",
                                                                icon: 'ic_action_name',
                                                                tag: unread[0]
                                                            }
                                                            },
                                                        topic: chatId.toString()
                                                        };
                                                    
                                                }
                                                    else {
                                                        message = {
                                                            data: {
                                                                userId: senderId,
                                                                chatId: chatId
                                                            },
                                                            android: {
                                                                notification: {
                                                                default_sound: true,
                                                                default_vibrate_timings: true,
                                                                imageUrl: image,
                                                                title: title,
                                                                body: body,
                                                                    channelId: "messages",
                                                                    icon: 'ic_action_name',
                                                                    tag: unread[0]
                                                                },
                                                                },
                                                            topic: chatId.toString()
                                                            };
                                                    }
                                            }
                                            else {
                                                if(teamImageSource == "MONOGRAM") {

                                                    message = {
                                                        data: {
                                                            teamId: teamId,
                                                            chatId: chatId
                                                        },
                                                        android: {
                                                            notification: {
                                                                default_sound: true,
                                                                default_vibrate_timings: true,
                                                                title: title,
                                                                body: body,
                                                                channelId: "messages",
                                                                icon: 'ic_action_name',
                                                                tag: senderId
                                                            }
                                                            },
                                                        topic: chatId.toString()
                                                        };
                                                    
                                                }
                                                    else {
                                                        message = {
                                                            data: {
                                                                teamId: teamId,
                                                                chatId: chatId
                                                            },
                                                            android: {
                                                                notification: {
                                                                default_sound: true,
                                                                default_vibrate_timings: true,
                                                                imageUrl: teamImage,
                                                                title: title,
                                                                body: body,
                                                                    channelId: "messages",
                                                                    icon: 'ic_action_name',
                                                                    tag: senderId
                                                                },
                                                                },
                                                            topic: chatId.toString()
                                                            };
                                                    }
                                                }
                                            
                                            
                        
                                            admin.messaging().send(message)
                                                .then(response => {
                                                    console.log("Notification sent successfully ("+response+"): ", message);
                                                })
                                                .catch(error => {
                                                    console.log('Error sending notification:', error);
                                                });
                                            }

                                    })
                                    .catch(error => {
                                        console.error("Error getting user", error);
                                    })
                                }
                            })
                            .catch(error => {
                                console.error("Error getting projects:", error);
                            });
                    }
                })
                .catch(error => {
                    console.error("Error getting tasks:", error);
                });
        }
    });
});





app.listen(port, () => {
    console.log(`Server is running on port ${port}`);
});
