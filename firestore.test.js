const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read schools or buses", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("schools").get());
  await assertFails(unauthDb.collection("buses").get());
});

test("Authenticated user: can create and query own school and bus", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  await assertSucceeds(
    aliceDb.collection("schools").doc("sch_a").set({
      userId: ALICE_UID,
      schoolCode: "SCH-0001",
      name: "School A - Delhi Public Academy",
      address: "Sector 14, Dwarka, New Delhi",
      ownerName: "Rajesh Sharma",
      ownerMobile: "+91 9811011223",
      loginId: "owner@schoola.edu.in",
      busCount: 4,
      isActive: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("schools").where("userId", "==", ALICE_UID).get()
  );
});

test("Authenticated user: cannot read another user's school or bus", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("schools").doc("bob_school").set({
      userId: BOB_UID,
      schoolCode: "SCH-0002",
      name: "Bob School",
      address: "MG Road",
      ownerName: "Bob",
      ownerMobile: "9999999999",
      loginId: "bob@school.in",
      busCount: 2,
      isActive: true,
      createdAt: now,
      updatedAt: now,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("schools").doc("bob_school").get());
});

test("Shadow update test: rejects ghost fields on school creation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  await assertFails(
    aliceDb.collection("schools").doc("sch_ghost").set({
      userId: ALICE_UID,
      schoolCode: "SCH-0001",
      name: "School A",
      address: "Address",
      ownerName: "Owner",
      ownerMobile: "9811011223",
      loginId: "owner@school.in",
      busCount: 2,
      isActive: true,
      isVerifiedSuperAdmin: true,
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Email spoofing test: unverified admin email cannot read another user's document", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("schools").doc("bob_school_2").set({
      userId: BOB_UID,
      schoolCode: "SCH-0002",
      name: "Bob School",
      address: "MG Road",
      ownerName: "Bob",
      ownerMobile: "9999999999",
      loginId: "bob@school.in",
      busCount: 2,
      isActive: true,
      createdAt: now,
      updatedAt: now,
    });
  });

  const spoofedAdminDb = testEnv
    .authenticatedContext("spoof_uid", {
      email: "rishusir5555@gmail.com",
      email_verified: false,
    })
    .firestore();
  await assertFails(spoofedAdminDb.collection("schools").doc("bob_school_2").get());
});
