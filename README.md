# coupon

1. client(vue.js + vuetify.js) <-> server(vert.x)
2. httpServerVerticle(core 개수) + couponVerticle(1 개) + couponIdGenVerticle(core 개수) + couponGenVerticle(1 개)
3. vert.x event bus                                                         

client
   |                    쿠폰생성확인         쿠폰id생성                쿠폰id등록         쿠폰email+id등록
httpServerVerticle --> couponVerticle --> couponIdGenVerticle --> couponGenVerticle --> couponVeticle
   |                                                                                           |
   |<------------------------------------------------------------------------------------------|
   
4. 쿠폰아이디 발행
- char[] characters = ['0','1','2',...,'a',...,'z','A',....,'Z']
- sha256(email)->string encode(64byte)->4byte 단위로 hashcode 를 이용해서 character 선택 

5. 빌드방법
 - 클라이언트
  1. cd client
  2. npm install
  3. npm run build
 
 - 서버
  1. mvn clean package

6. 실행
java -jar target\coupon-io-0.0.1-SNAPSHOT-fat.jar
