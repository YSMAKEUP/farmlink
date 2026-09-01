// k6/farm-load-test.js
// 목적: 고정 TPS가 아니라 VU를 점진적으로 올리면서 시스템이 어디서 무너지는지
// (에러율 급증, 응답시간 급증) 찾는 스트레스 테스트.
// 실행: k6 run k6/farm-load-test.js
import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// 미리 회원가입해둔 테스트 계정으로 바꿔줘
// (해당 계정 농장에 소/착유기록/작업일지/번식기록 시드 데이터가 있어야 병목이 제대로 드러남)
const TEST_EMAIL = __ENV.TEST_EMAIL || 'loadtest@farmlink.com';
const TEST_PASSWORD = __ENV.TEST_PASSWORD || 'test1234!';

export const options = {
  scenarios: {
    farm_stress: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { target: 20, duration: '1m' },   // 0 -> 20명
        { target: 20, duration: '1m' },   // 20명 유지
        { target: 50, duration: '1m' },   // 20 -> 50명
        { target: 50, duration: '1m' },   // 50명 유지
        { target: 100, duration: '1m' },  // 50 -> 100명
        { target: 100, duration: '1m' },  // 100명 유지
        { target: 200, duration: '1m' },  // 100 -> 200명
        { target: 200, duration: '1m' },  // 200명 유지
        { target: 0, duration: '30s' },   // 램프다운
      ],
      gracefulRampDown: '10s',
    },
  },
  thresholds: {
    // abortOnFail은 안 걸어둠 - 중간에 중단시키지 않고 끝까지 돌려서
    // 결과(http_req_duration{name:...})에서 어느 엔드포인트가 먼저 튀는지 직접 확인하는 용도
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<500'],
  },
};

function pad(n) {
  return String(n).padStart(2, '0');
}

function toDateStr(d) {
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

function randomIntBetween(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

export function setup() {
  const res = http.post(
    `${BASE_URL}/api/users/login`,
    JSON.stringify({ email: TEST_EMAIL, password: TEST_PASSWORD }),
    { headers: { 'Content-Type': 'application/json' } }
  );
  check(res, { '로그인 성공': (r) => r.status === 200 });
  return { token: res.json('accessToken') };
}

export default function (data) {
  const headers = { Authorization: `Bearer ${data.token}` };
  const now = new Date();

  const end = toDateStr(now);
  const startDate = new Date(now);
  startDate.setDate(startDate.getDate() - 30);
  const start = toDateStr(startDate);

  const year = now.getFullYear();
  const month = now.getMonth() + 1;
  const noteDate = `${year}-${pad(month)}-${pad(randomIntBetween(1, 28))}`;

  // 실제 사용 패턴처럼 여러 조회 API를 한 번의 세션에서 섞어서 호출.
  // 각 요청은 tags.name으로 구분해서 k6 결과에서 어느 엔드포인트가
  // 먼저 느려지는지/실패하는지 따로 볼 수 있게 함.
  let res;

  res = http.get(`${BASE_URL}/api/cows?page=0&size=20`, {
    headers,
    tags: { name: 'cows_list' },
  });
  check(res, { 'cows 200': (r) => r.status === 200 });
  sleep(randomIntBetween(1, 2));

  res = http.get(`${BASE_URL}/api/milk-records?start=${start}&end=${end}`, {
    headers,
    tags: { name: 'milk_records' },
  });
  check(res, { 'milk-records 200': (r) => r.status === 200 });
  sleep(randomIntBetween(1, 2));

  res = http.get(
    `${BASE_URL}/api/worklog/search?started=${start}T00:00:00&ended=${end}T23:59:59`,
    { headers, tags: { name: 'worklog_search' } }
  );
  check(res, { 'worklog 200': (r) => r.status === 200 });
  sleep(randomIntBetween(1, 2));

  res = http.get(`${BASE_URL}/api/breeding-records/month?year=${year}&month=${month}`, {
    headers,
    tags: { name: 'breeding_month' },
  });
  check(res, { 'breeding month 200': (r) => r.status === 200 });
  sleep(randomIntBetween(1, 2));

  res = http.get(`${BASE_URL}/api/breeding-records`, {
    headers,
    tags: { name: 'breeding_all' },
  });
  check(res, { 'breeding all 200': (r) => r.status === 200 });
  sleep(randomIntBetween(1, 2));

  res = http.get(`${BASE_URL}/api/calendar-notes?date=${noteDate}`, {
    headers,
    tags: { name: 'calendar_notes' },
  });
  check(res, { 'calendar-notes 200': (r) => r.status === 200 });
  sleep(randomIntBetween(1, 3));
}
