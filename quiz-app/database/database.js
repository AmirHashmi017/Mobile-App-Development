import { openDatabaseSync } from 'expo-sqlite';

const db = openDatabaseSync('ecat_quiz.db');

const executeQuery = (sql, params = [], isWrite = false) => {
  try {
    if (isWrite) {
      return db.runSync(sql, params);
    } else {
      return db.getAllSync(sql, params);
    }
  } catch (error) {
    console.error('SQL Error:', sql, error);
    throw error;
  }
};

const initializeDatabase = async () => {
  const schema = [
    `CREATE TABLE IF NOT EXISTS users (
      id TEXT PRIMARY KEY NOT NULL,
      email TEXT UNIQUE NOT NULL,
      password TEXT NOT NULL,
      name TEXT NOT NULL,
      role TEXT NOT NULL
    )`,
    `CREATE TABLE IF NOT EXISTS quizzes (
      id TEXT PRIMARY KEY NOT NULL,
      title TEXT NOT NULL,
      teacher_id TEXT NOT NULL,
      published INTEGER DEFAULT 0,
      created_at TEXT DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (teacher_id) REFERENCES users(id)
    )`,
    `CREATE TABLE IF NOT EXISTS questions (
      id TEXT PRIMARY KEY NOT NULL,
      quiz_id TEXT NOT NULL,
      text TEXT NOT NULL,
      FOREIGN KEY (quiz_id) REFERENCES quizzes(id)
    )`,
    `CREATE TABLE IF NOT EXISTS answers (
      id TEXT PRIMARY KEY NOT NULL,
      question_id TEXT NOT NULL,
      text TEXT NOT NULL,
      correct INTEGER DEFAULT 0,
      FOREIGN KEY (question_id) REFERENCES questions(id)
    )`,
    `CREATE TABLE IF NOT EXISTS results (
      id TEXT PRIMARY KEY NOT NULL,
      quiz_id TEXT NOT NULL,
      student_id TEXT NOT NULL,
      score INTEGER NOT NULL,
      total_questions INTEGER NOT NULL,
      date TEXT DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (quiz_id) REFERENCES quizzes(id),
      FOREIGN KEY (student_id) REFERENCES users(id)
    )`
  ];

  try {
    for (const query of schema) {
      executeQuery(query, [], true);
    }
    console.log('Database schema initialized');
    return true;
  } catch (error) {
    console.error('Database initialization failed:', error);
    throw error;
  }
};

const initializeTestData = () => {
  try {
    const result = executeQuery('SELECT COUNT(*) as count FROM users');
    if (result[0].count === 0) {
      executeQuery(
        'INSERT INTO users (id, email, password, name, role) VALUES (?, ?, ?, ?, ?)',
        ['1', 'teacher@ecat.com', 'password123', 'Test Teacher', 'teacher'],
        true
      );
      executeQuery(
        'INSERT INTO users (id, email, password, name, role) VALUES (?, ?, ?, ?, ?)',
        ['2', 'student@ecat.com', 'password123', 'Test Student', 'student'],
        true
      );
      console.log('Test data initialized');
    }
    return true;
  } catch (error) {
    console.error('Failed to initialize test data:', error);
    throw error;
  }
};

const database = {
  isInitialized: false,
  
  ensureInitialized() {
    if (!this.isInitialized) {
      this.initializeDatabase();
      this.isInitialized = true;
    }
  },

  initializeDatabase,
  initializeTestData,

  addUser: (user) => {
    database.ensureInitialized();
    const { id, email, password, name, role } = user;
    executeQuery(
      'INSERT INTO users (id, email, password, name, role) VALUES (?, ?, ?, ?, ?)',
      [id, email, password, name, role],
      true
    );
    return user;
  },

  getUserByEmail: (email) => {
    database.ensureInitialized();
    const result = executeQuery(
      'SELECT * FROM users WHERE email = ?',
      [email]
    );
    return result[0];
  },

  addQuiz: (quiz) => {
    database.ensureInitialized();
    const { id, title, teacherId, published, questions } = quiz;
    executeQuery(
      'INSERT INTO quizzes (id, title, teacher_id, published) VALUES (?, ?, ?, ?)',
      [id, title, teacherId, published ? 1 : 0],
      true
    );

    for (const question of questions) {
      const questionId = Math.random().toString(36).substring(7);
      executeQuery(
        'INSERT INTO questions (id, quiz_id, text) VALUES (?, ?, ?)',
        [questionId, id, question.text],
        true
      );

      for (const answer of question.answers) {
        executeQuery(
          'INSERT INTO answers (id, question_id, text, correct) VALUES (?, ?, ?, ?)',
          [Math.random().toString(36).substring(7), questionId, answer.text, answer.correct ? 1 : 0],
          true
        );
      }
    }
    return quiz;
  },

  getQuizById: (id) => {
    const quizResult = executeQuery(
      'SELECT * FROM quizzes WHERE id = ?',
      [id]
    );
    if (!quizResult[0]) return null;

    const questionsResult = executeQuery(
      'SELECT * FROM questions WHERE quiz_id = ?',
      [id]
    );

    const questions = questionsResult.map((question) => {
      const answersResult = executeQuery(
        'SELECT * FROM answers WHERE question_id = ?',
        [question.id]
      );
      return {
        ...question,
        answers: answersResult.map(a => ({
          ...a,
          correct: a.correct === 1
        }))
      };
    });

    return {
      ...quizResult[0],
      published: quizResult[0].published === 1,
      questions
    };
  },

  getQuizzesByTeacher: (teacherId) => {
    const result = executeQuery(
      'SELECT * FROM quizzes WHERE teacher_id = ?',
      [teacherId]
    );
    return result.map(q => ({
      ...q,
      published: q.published === 1
    }));
  },

  getAllPublishedQuizzes: () => {
    database.ensureInitialized();

    const quizzes = executeQuery(
      'SELECT * FROM quizzes WHERE published = 1'
    );

    return quizzes.map(quiz => {
      const questions = executeQuery(
        'SELECT * FROM questions WHERE quiz_id = ?',
        [quiz.id]
      );

      const enrichedQuestions = questions.map(question => {
        const answers = executeQuery(
          'SELECT * FROM answers WHERE question_id = ?',
          [question.id]
        );

        return {
          ...question,
          answers: answers.map(a => ({
            ...a,
            correct: a.correct === 1
          }))
        };
      });

      return {
        ...quiz,
        published: quiz.published === 1,
        questions: enrichedQuestions || []
      };
    });
  },

  
  updateQuiz: (quiz) => {
    const { id, title, published, questions } = quiz;
    executeQuery(
      'UPDATE quizzes SET title = ?, published = ? WHERE id = ?',
      [title, published ? 1 : 0, id],
      true
    );

    executeQuery(
      'DELETE FROM answers WHERE question_id IN (SELECT id FROM questions WHERE quiz_id = ?)',
      [id],
      true
    );
    executeQuery(
      'DELETE FROM questions WHERE quiz_id = ?',
      [id],
      true
    );

    for (const question of questions) {
      const questionId = Math.random().toString(36).substring(7);
      executeQuery(
        'INSERT INTO questions (id, quiz_id, text) VALUES (?, ?, ?)',
        [questionId, id, question.text],
        true
      );

      for (const answer of question.answers) {
        executeQuery(
          'INSERT INTO answers (id, question_id, text, correct) VALUES (?, ?, ?, ?)',
          [Math.random().toString(36).substring(7), questionId, answer.text, answer.correct ? 1 : 0],
          true
        );
      }
    }
    return quiz;
  },

  deleteQuiz: (id) => {
    executeQuery(
      'DELETE FROM answers WHERE question_id IN (SELECT id FROM questions WHERE quiz_id = ?)',
      [id],
      true
    );
    executeQuery(
      'DELETE FROM questions WHERE quiz_id = ?',
      [id],
      true
    );
    executeQuery(
      'DELETE FROM quizzes WHERE id = ?',
      [id],
      true
    );
  },

  addResult: (result) => {
    const { quizId, studentId, score, totalQuestions } = result;
    executeQuery(
      'INSERT INTO results (id, quiz_id, student_id, score, total_questions) VALUES (?, ?, ?, ?, ?)',
      [Math.random().toString(36).substring(7), quizId, studentId, score, totalQuestions],
      true
    );
    return result;
  },

  getResultsByStudent: (studentId) => {
    const result = executeQuery(
      'SELECT * FROM results WHERE student_id = ?',
      [studentId]
    );
    return result;
  }
};

export default database;