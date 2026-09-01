// k6/seed-dummy-data.js
// 목적: 부하테스트용 더미 데이터 생성 (소 30마리 + 착유기록/작업일지/번식기록/캘린더메모).
// 부하테스트가 아니라 1회성 시드 스크립트라 VU/executor 설정 없이 기본값(1 VU, 1회 실행)으로 돌아감.
// 실행: k6 run k6/seed-dummy-data.js
import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const TEST_EMAIL = __ENV.TEST_EMAIL || 'loadtest@farmlink.com';
const TEST_PASSWORD = __ENV.TEST_PASSWORD || 'test1234!';

const COW_COUNT = Number(__ENV.COW_COUNT || 30);
const BREEDS = ['홀스타인', '저지', '브라운스위스'];
const COW_STATUSES = ['MILKING', 'MILKING', 'MILKING', 'DRY', 'HEIFER'];
const WORK_TYPES = ['FEED', 'TREATMENT', 'QUARANTINE', 'ETC'];
const CHECK_RESULTS = ['SUCCESS', 'FAIL']; // WAITING은 기본값이라 일부러 안 건드림

const jsonHeaders = (token) => ({
  'Content-Type': 'application/json',
  Authorization: `Bearer ${token}`,
});

function pad(n) {
  return String(n).padStart(2, '0');
}

function toDateStr(d) {
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

function daysAgo(n) {
  const d = new Date();
  d.setDate(d.getDate() - n);
  return d;
}

function randomIntBetween(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function pick(arr) {
  return arr[randomIntBetween(0, arr.length - 1)];
}

export default function () {
  // 1. 로그인
  const loginRes = http.post(
    `${BASE_URL}/api/users/login`,
    JSON.stringify({ email: TEST_EMAIL, password: TEST_PASSWORD }),
    { headers: { 'Content-Type': 'application/json' } }
  );
  check(loginRes, { '로그인 성공': (r) => r.status === 200 });
  if (loginRes.status !== 200) {
    console.error('로그인 실패 - 계정이 없거나 비밀번호가 다름. 먼저 회원가입 필요:', loginRes.body);
    return;
  }
  const token = loginRes.json('accessToken');
  const headers = jsonHeaders(token);
  const runTag = Date.now().toString().slice(-6); // 재실행해도 이표번호 안 겹치게

  // 2. 소 등록
  const cowIds = [];
  for (let i = 0; i < COW_COUNT; i++) {
    const birth = daysAgo(randomIntBetween(365, 365 * 6));
    const body = {
      earTagNumber: `SEED-${runTag}-${pad(i)}`,
      name: `젖소${i + 1}`,
      breed: pick(BREEDS),
      birthDate: toDateStr(birth),
      parity: randomIntBetween(0, 4),
      status: pick(COW_STATUSES),
    };
    const res = http.post(`${BASE_URL}/api/cows`, JSON.stringify(body), { headers });
    if (res.status === 201) {
      cowIds.push(res.json('id'));
    } else {
      console.error(`소 등록 실패 (${i}):`, res.status, res.body);
    }
  }
  console.log(`소 ${cowIds.length}마리 등록 완료`);

  // 3. 마리당 착유기록/작업일지/번식기록 + 일부 캘린더 메모
  let milkCount = 0;
  let workLogCount = 0;
  let breedingCount = 0;
  let checkedCount = 0;
  let noteCount = 0;

  cowIds.forEach((cowId) => {
    // 착유기록: 최근 30일, 하루 1~2세션
    for (let d = 0; d < 30; d++) {
      const date = toDateStr(daysAgo(d));
      const sessions = Math.random() < 0.7 ? ['MORNING', 'EVENING'] : ['MORNING'];
      sessions.forEach((session) => {
        const res = http.post(
          `${BASE_URL}/api/milk-records`,
          JSON.stringify({
            cowId,
            milkDate: date,
            session,
            amount: Math.round((15 + Math.random() * 20) * 10) / 10,
          }),
          { headers }
        );
        if (res.status === 200) milkCount++;
      });
    }

    // 작업일지: 마리당 2~4건, 최근 30일 내 랜덤 시각
    const workLogN = randomIntBetween(2, 4);
    for (let w = 0; w < workLogN; w++) {
      const dt = daysAgo(randomIntBetween(0, 30));
      const workDateTime = `${toDateStr(dt)}T${pad(randomIntBetween(6, 18))}:00:00`;
      const res = http.post(
        `${BASE_URL}/api/worklog/create`,
        JSON.stringify({
          cowId,
          workType: pick(WORK_TYPES),
          workDateTime,
          content: '더미 작업일지 데이터',
        }),
        { headers }
      );
      if (res.status === 200 || res.status === 201) workLogCount++;
    }

    // 번식기록: 마리당 1건, 인공수정일 30~250일 전 (dueDate가 과거/미래에 걸치게)
    const inseminationDate = toDateStr(daysAgo(randomIntBetween(30, 250)));
    const createRes = http.post(
      `${BASE_URL}/api/breeding-records/create`,
      JSON.stringify({
        cowId,
        inseminationDate,
        semenCode: `SEMEN-${randomIntBetween(100, 999)}`,
        technicianName: '더미기술자',
        note: '더미 번식기록 데이터',
      }),
      { headers }
    );
    if (createRes.status === 200 || createRes.status === 201) {
      breedingCount++;
      // 60% 확률로 임신감정 결과까지 채워둠 (프론트 뱃지/통계/필터가 WAITING만 있으면 심심함)
      if (Math.random() < 0.6) {
        const recordId = createRes.json('id');
        if (recordId) {
          const patchRes = http.patch(
            `${BASE_URL}/api/breeding-records/${recordId}/check`,
            JSON.stringify({ checkResult: pick(CHECK_RESULTS) }),
            { headers }
          );
          if (patchRes.status === 200) checkedCount++;
        }
      }
    }

    // 캘린더 메모: 마리당 30% 확률로 1건
    if (Math.random() < 0.3) {
      const memoDate = toDateStr(daysAgo(randomIntBetween(0, 30)));
      const res = http.post(
        `${BASE_URL}/api/calendar-notes`,
        JSON.stringify({
          title: '더미 메모',
          cowId,
          memoDate,
          content: '더미 캘린더 메모 데이터',
        }),
        { headers }
      );
      if (res.status === 200) noteCount++;
    }
  });

  console.log(
    `착유기록 ${milkCount}건 / 작업일지 ${workLogCount}건 / 번식기록 ${breedingCount}건(임신감정 ${checkedCount}건) / 캘린더메모 ${noteCount}건 생성 완료`
  );
}
