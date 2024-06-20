const admin = require('firebase-admin');
const express = require('express');
const serviceAccount = require('./config/serviceAccountKey.json');
const { QuerySnapshot } = require('firebase-admin/firestore');

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();
const app = express();
const port = 3000;

let isInitialSnapshotProcessed = false;

// Listen for Firestore changes
db.collection('history').onSnapshot(snapshot => {
    if (!isInitialSnapshotProcessed) {
        // Skip the initial snapshot
        isInitialSnapshotProcessed = true;
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

                                    
                                    const message = {
                                        notification: {
                                            title: projectName + " - " + taskName,
                                            body: `${newValue.text}`,
                                        },
                                        topic: taskId.toString()
                                    };
                
                                    admin.messaging().send(message)
                                        .then(response => {
                                            console.log('Notification sent successfully:', response);
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

// Function to sanitize taskId to ensure it conforms to topic naming conventions
function sanitizeTopicName(taskId) {
    // Replace any characters that are not allowed in topic names
    return taskId.replace(/[^a-zA-Z0-9\-_.]/g, '_');
}

app.listen(port, () => {
    console.log(`Server is running on port ${port}`);
});
