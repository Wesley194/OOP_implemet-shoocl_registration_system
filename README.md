# School Registration System / 學校選課系統

[English Version](#english-version) | [中文版](#中文版)

---

## English Version

This is an Object-Oriented Programming (OOP) project developed with **Java** and **SQLite**. The system simulates a real-world university course registration mechanism, featuring a complete Graphical User Interface (GUI), persistent database storage, multiple user roles, and a waitlist lottery system.

### Core Features

The system categorizes users into three roles: "Admin", "Teacher", and "Student", with the following features:

#### Common Features

- **Profile Management**: Users can update their name and login password.

#### Admin

- **Superuser Login**: Independent admin account to create teacher and student accounts.
- **System State Control**: Switch global enrollment phases (e.g., Closed, Pre-Enrollment, Add/Drop, Lottery phase).

#### Teacher

- **Course Management**: Create new courses. The system automatically binds the instructor and writes records to the database.
- **Auth Code Distribution**: Automatically generates unique "One-Time Password (OTP) Auth Codes" for each course, allowing professors to distribute them for special enrollments (add codes).
- **Announcement System**: Publish important announcements for their specific courses to notify enrolled students.
- **Grade Registration**: View the enrolled student list and input or modify final grades.

#### Student

- **Course Search**: Supports keyword fuzzy search by "Course ID", "Course Name", or "Instructor Name".
- **Enroll & Drop**: Add or drop courses during valid enrollment phases.
- **Waitlist & Lottery**: Register for the waitlist (pending status), awaiting the system lottery run.
- **Auth Code Enrollment**: Directly enroll using a professor's auth code. Features a "single-use" security mechanism to prevent code sharing.
- **Drop Courses**: Remove successfully enrolled courses from the official schedule.
- **View Announcements**: Read announcements posted by professors for enrolled courses.
- **Personal Schedule & Transcript**: View enrolled courses, class times, registered grades, and GPA.

#### Error Handling

- **Duplicate Course Prevention**: Blocks the creation of courses with duplicate IDs.
- **Enrollment Limits**: Prevents "duplicate enrollment" and "duplicate waitlist registration".
- **Time Conflict Prevention**: Compares `TimeSlot`s and throws an Exception if schedules overlap, aborting the enrollment process.
- **Multi-layered Auth Code Validation**: Intercepts abnormal behaviors with error messages such as "Invalid Code", "Code Not for this Course", "Code Already Used", and "Time Conflict".
- **Capacity Limit**: Checks the `isFull()` state in memory to prevent over-enrollment.

### Tech Stack

- **Programming Language**: Java with OOP design (Encapsulation, Inheritance, Polymorphism)
- **GUI**: Java Swing (`MainGUI`)
- **Relational Database**: SQLite (via JDBC connection)

### Database Schema

| Table Name        | Description                                         | Relationships                            |
| ----------------- | --------------------------------------------------- | ---------------------------------------- |
| `admins`          | Stores super admin data                             | Independent                              |
| `students`        | Stores basic student data (ID, name, password)      | Independent                              |
| `teachers`        | Stores basic teacher data (ID, name, password)      | Independent                              |
| `system_settings` | Stores global system states (e.g., current phase)   | Independent                              |
| `courses`         | Stores course info (ID, name, credits, schedule)    | `teacher_id` (FK)                        |
| `enrollments`     | Stores official enrollments & grades (Many-to-Many) | `student_id`, `course_id` (Composite PK) |
| `pending`         | Stores waitlist applications                        | `student_id`, `course_id` (Composite PK) |
| `auth_codes`      | Manages one-time auth codes and usage state         | `course_id`, `used_by` (FK)              |
| `announcements`   | Stores course announcements                         | `course_id` (FK)                         |

### Getting Started

#### 1. Environment Setup

Please ensure the **SQLite JDBC Driver** (`sqlite-jdbc.jar`) is included in your project dependencies.

#### 2. Generate Test Data (Highly Recommended)

For the first run, execute the testing script. The system will automatically build the latest database schema and generate students, professors, courses, auth codes, and simulated enrollment records.

- **Execution File**: `DatabaseTester.java` _(Note: If an old .db file exists, please delete it manually first)_

#### 3. Launch System

- **GUI**: Run `MainGUI.java`

#### Test Accounts

If `DatabaseTester` was executed, you can log in using:

- **Admin**: `admin` (Password: `admin123`)
- **Teacher**: `T001` ~ `T010` (Password for all: `1234`)
- **Student**: `B001` ~ `B050` (Password for all: `0000`)

<br><br>

---

## 中文版

這是一個基於 **Java** 與 **SQLite** 開發的物件導向 (OOP) 專案。系統模擬了真實大學的選課機制，包含完整的圖形化介面 (GUI)、資料庫持久化儲存、多重使用者身分，以及選課與排隊機制。

### 核心功能

系統分為「管理員」、「教師」與「學生」三種使用者身分，並具備以下功能：

#### 通用功能

- **個人資料管理**：使用者可更新自己的姓名與登入密碼。

#### 管理員 (Admin)

- **最高權限登入**：獨立的管理員帳號，可新增教師和學生的帳號。
- **系統狀態控制**：切換全校選課階段（如：關閉系統、預選、加退選、抽籤階段）。

#### 教師 (Teacher)

- **課程管理**：可開設新課程，系統會自動綁定授課教師並將紀錄寫入資料庫。
- **密碼卡發放**：系統為每門課程自動生成專屬的一次性密碼卡，供教授發放給學生進行特殊加簽。
- **課程公告系統**：可針對自己開設的課程發布重要公告，通知修課學生。
- **登記成績**：查看所開課程的修課學生名單，並為學生登記或修改期末成績。

#### 學生 (Student)

- **課程搜尋**：支援關鍵字模糊搜尋，可透過「課程代碼」、「課程名稱」或「授課教師姓名」篩選課程。
- **選課與退選**：在選課階段進行加選與退選。
- **抽籤排隊**：除了直接選上，亦支援將選課登記進入候補抽籤名單。
- **密碼卡加簽**：輸入教授發放的密碼卡可直接加選。具備「用過即註銷」的安全機制，防止密碼外流重複使用。
- **退選**：允許學生將已選上的課程從正式課表中移除。
- **查看課程公告**：瀏覽所選課程的教授公告。
- **個人課表與成績單**：查看已選課程、上課時間，以及教師已登記的成績與 GPA。

#### Error handling (異常處理)

- **開課檢測**：攔截課程代碼重複建立的問題。
- **選課限制**：阻擋「重複加選」與「重複登記抽籤」。
- **衝堂攔截**：比對 TimeSlot，若發生時間重疊則拋出 Exception 中斷選課流程。
- **密碼卡多重驗證**：拋出錯誤訊息攔截「無效密碼」、「非該課專屬密碼」、「密碼卡已被使用」 與 「衝堂攔截」等異常加簽行為。
- **滿員阻擋**：判斷記憶體內的 `isFull()` 狀態，防止課程超收。

### 技術架構

- **程式語言**：Java 運用物件導向設計 (封裝、繼承、多型)
- **圖形介面**：Java Swing (`MainGUI`)
- **關聯式資料庫**：SQLite (透過 JDBC 連線)

### 資料庫設計

系統底層使用關聯表來維持複雜的業務邏輯：

| 表格名稱          | 說明                                      | 關聯性                                   |
| ----------------- | ----------------------------------------- | ---------------------------------------- |
| `admins`          | 儲存最高管理員資料                        | 獨立                                     |
| `students`        | 儲存學生基本資料 (學號、姓名、密碼)       | 獨立                                     |
| `teachers`        | 儲存教師基本資料 (員工編號、姓名、密碼)   | 獨立                                     |
| `system_settings` | 儲存系統全域狀態與設定 (如：當前選課階段) | 獨立                                     |
| `courses`         | 儲存課程資訊 (代碼、名稱、學分、上課時間) | `teacher_id` (FK)                        |
| `enrollments`     | 儲存正式選課紀錄與成績 (多對多中介表)     | `student_id`, `course_id` (Composite PK) |
| `pending`         | 儲存等待抽籤的候補名單                    | `student_id`, `course_id` (Composite PK) |
| `auth_codes`      | 管理一次性加簽密碼卡與使用狀態            | `course_id`, `used_by` (FK)              |
| `announcements`   | 儲存各課程發布的公告                      | `course_id` (FK)                         |

### 如何執行

#### 1. 環境準備

請確保專案中已經引入了 **SQLite JDBC Driver** (`sqlite-jdbc.jar`)。

#### 2. 生成測試資料 (強烈建議)

首次執行時，請先執行測試腳本。系統會自動建立最新架構的資料庫，並隨機生成學生、教授、課程、一次性密碼卡，以及模擬選課與排隊紀錄。

- **執行檔案**：`DatabaseTester.java` _(注意：若有舊的 db 檔案請先手動刪除)_

#### 3. 啟動系統

- **圖形化介面 (GUI)**：執行 `MainGUI.java`

#### 測試帳號提示

若有執行 `DatabaseTester`，可使用以下帳號登入：

- **管理員帳號**：`admin` (密碼：`admin123`)
- **教授帳號**：`T001` ~ `T010` (密碼統一為：`1234`)
- **學生帳號**：`B001` ~ `B050` (密碼統一為：`0000`)
