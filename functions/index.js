const functions = require("firebase-functions/v1"); // Compatibilitate v1
const admin = require("firebase-admin");
const fetch = require("node-fetch");
admin.initializeApp();

const WEATHER_API_KEY = "a341b7bd650d4efe97b152901250205";
const BASE_URL = "https://api.weatherapi.com/v1";

exports.checkPlantsToWater = functions.pubsub.schedule("every 24 hours").onRun(async (context) => {
    console.log("🔔 Function execution started!");

    const db = admin.firestore();
    const usersSnapshot = await db.collection("users").get();

    for (const userDoc of usersSnapshot.docs) {
        const userId = userDoc.id;
        const userLocation = userDoc.data().location || "Bucharest";
        console.log(`👤 Processing user ${userId}, location: ${userLocation}`);

        const plantsSnapshot = await db.collection("users").doc(userId).collection("myPlants").get();
        const now = new Date();
        const plantIntervals = [];

        for (const plantDoc of plantsSnapshot.docs) {
            const plant = plantDoc.data();
            const plantId = plantDoc.id;

            if (!plant.plantData || !plant.plantData.wateringInterval || !plant.lastWateredDate) {
                console.log(`⚠️ Skipping plant ${plantId} - missing data`);
                continue;
            }

            const intervalStr = plant.plantData.wateringInterval.replace(/[^0-9\-]/g, "-");
            const parts = intervalStr.split("-");
            const minDays = parseInt(parts[0].trim());
            const maxDays = parts[1] ? parseInt(parts[1].trim()) : minDays;

            const lastWateredDate = new Date(plant.lastWateredDate);
            const daysSince = Math.floor((now - lastWateredDate) / (1000 * 60 * 60 * 24));

            let start = minDays - daysSince;
            let end = maxDays - daysSince;
            start = Math.max(0, start);
            end = Math.min(30, end);


            if (start > 30) continue;

            plantIntervals.push({ plant, plantId, start, end });
            console.log(`🌿 Plant ${plant.customName || plant.plantData.commonName} (ID: ${plantId}), start: ${start}, end: ${end}`);
        }

        plantIntervals.sort((a, b) => a.end - b.end);
        let i = 0;
        const nowDate = new Date();

        while (i < plantIntervals.length) {
            const cluster = [];
            let groupEnd = plantIntervals[i].end;
            cluster.push(plantIntervals[i]);
            i++;

            while (i < plantIntervals.length && plantIntervals[i].start <= groupEnd) {
                cluster.push(plantIntervals[i]);
                groupEnd = Math.min(groupEnd, plantIntervals[i].end);
                i++;
            }

            let chosenDay = cluster.length === 1
                ? Math.floor((cluster[0].start + cluster[0].end) / 2)
                : groupEnd;

            if (groupEnd < 0) chosenDay = 0;

            const chosenDate = new Date(nowDate);
            chosenDate.setDate(nowDate.getDate() + chosenDay);

            if (chosenDay <= 0) {
                for (const { plant, plantId } of cluster) {
                    console.log(`📢 Plant ${plant.customName || plant.plantData.commonName} (ID: ${plantId}) needs watering today or missed.`);

                    if (isExposedToRain(plant)) {
                        console.log(`🌧️ Checking rain exposure for plant ${plantId}`);
                        const rainData = await getHistoricalRainfall(userLocation, 7);
                        const rainDate = getEffectiveRainDate(rainData);
                        if (rainDate && plant.lastWateredDate !== rainDate) {
                            await db.collection("users").doc(userId).collection("myPlants").doc(plantId)
                                .update({ lastWateredDate: rainDate });
                            console.log(`✅ Updated ${plant.customName || plant.plantData.commonName} with rain date ${rainDate}`);
                            continue;
                        } else {
                            console.log(`🚫 No significant rain for plant ${plantId} or date already set`);
                        }
                    }

                    await sendNotification(userId, plant);
                    console.log(`🚀 Notification sent for plant ${plant.customName || plant.plantData.commonName}`);
                }
            } else {
                console.log(`⏳ Plant cluster postponed (chosenDay=${chosenDay})`);
            }
        }
    }

    console.log("✅ Function execution completed.");
    return null;
});

async function sendNotification(userId, plant) {
    const payload = {
        notification: {
            title: "🌱 Plant Care Reminder",
            body: `The plant "${plant.customName || plant.plantData.commonName}" needs watering today!`,
        },
        topic: userId,
    };
    await admin.messaging().send(payload);
}

function isExposedToRain(plant) {
    const site = plant.addedSite || "";
    return plant.exposedToRain || site.toLowerCase() === "front yard" || site.toLowerCase() === "backyard";
}

async function getHistoricalRainfall(location, days) {
    const result = {};
    const now = new Date();
    for (let i = 0; i < days; i++) {
        const date = now.toISOString().split("T")[0];
        const url = `${BASE_URL}/history.json?key=${WEATHER_API_KEY}&q=${location}&dt=${date}`;
        try {
            const response = await fetch(url);
            const data = await response.json();
            const precip = data.forecast.forecastday[0].day.totalprecip_mm;
            result[date] = precip;
        } catch (error) {
            console.error(`🌧️ Error fetching weather data for ${date}:`, error);
        }
        now.setDate(now.getDate() - 1);
    }
    return result;
}

function getEffectiveRainDate(rainData) {
    const dates = Object.keys(rainData).sort();
    for (let i = 0; i < dates.length; i++) {
        const val = rainData[dates[i]];
        if (val >= 20) return dates[i];
        if (i < dates.length - 1 && val > 10 && rainData[dates[i + 1]] > 10) return dates[i + 1];
    }
    return null;
}
