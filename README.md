<img width="120" src="https://github.com/user-attachments/assets/e0c8861b-77f5-4878-85ad-0e6308fa2814" />

# SMASHING

<p align="center">
<img width="5760" height="3240"  src="https://github.com/user-attachments/assets/56258869-67c6-4a17-adc5-9fc4c4946536" />
</p>

#### **스포츠인을 위한 매칭 시스템은 계속된다, 스매싱**

스매싱은 2030 세대의 스포츠 자아 완성을 돕는 게이미피케이션 기반 *라켓 스포츠 매칭 플랫폼*입니다.

주요 기능으로 홈 화면에서 곧 다가오는 매칭부터 사용자 맞춤 추천, 동네 랭킹까지 다양한 기능을 만날 수 있습니다.

실시간 알림을 통해 '받은 매칭', '보낸 매칭', '매칭 확정' 탭으로 간편하게 이동하며 관리할 수 있습니다.

경기 종료 후 점수와 후기를 남기고, 매칭 결과 확인 시 결과를 승인하거나 반려할 수 있습니다.

<br> 

## CONTRIBUTORS

|                        김민경 (Lead) <br/>[@kyoooooong](https://github.com/kyoooooong)                         |                             이유빈<br/>[@leeeyubin](https://github.com/leeeyubin)                              |
|:-----------------------------------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------------------------------:|
| <img width="200px" src="https://github.com/user-attachments/assets/91bf4ef6-9e38-4f7f-852e-c7c202e104cb" /> | <img width="200px" src="https://github.com/user-attachments/assets/e91027e6-425c-4a5f-9d7c-0ea63b2a3816" /> |

## TECH STACK

![Java](https://img.shields.io/badge/JDK-21-007396cb?style=flat&logo=openjdk&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.1.21-7F52FF?style=flat&logo=kotlin&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?style=flat&logo=springboot&logoColor=white)

| Category                  | Stack                                  |
|---------------------------|----------------------------------------|
| **Infra , Cloud**         | AWS (EC2, RDS, S3, ECR), Docker, Nginx |
| **CI/CD , Collaboration** | GitHub Actions, Jenkins, CodeRabbit    |
| **Security , Auth**       | Spring Security, JWT, OAuth 2.0        |
| **Data , Cache**          | MySQL, Redis                           |
| **Docs , Messaging**      | Swagger, FCM, APNs                     |

## PROJECT STRUCTURE

```
🗃️ org.appjam.smashing
├─ 📂 domain
│  ├─ 📂 auth
│  ├─ 📂 common.entity
│  ├─ 📂 game
│  ├─ 📂 lp
│  ├─ 📂 matching
│  ├─ 📂 notification
│  ├─ 📂 outbox
│  ├─ 📂 review
│  ├─ 📂 sport
│  ├─ 📂 tier
│  └─ 📂 user
├─ 📂 global
│  ├─ 📂 auth
│  ├─ 📂 common
│  ├─ 📂 config
│  ├─ 📂 exception
│  ├─ 📂 extensions
│  └─ 📂 util
└─ 🗂️ SmashingApplication

```

## ERD

<img width="2180" height="2192" alt="SMASHING_ERD" src="https://github.com/user-attachments/assets/397784e9-af73-45a4-a74c-d275422f3593" />

