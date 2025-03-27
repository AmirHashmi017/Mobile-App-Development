
import { openDatabaseSync } from 'expo-sqlite/next';

// Open database synchronously
const db = openDatabaseSync('ecat_quiz.db');

const initializeDatabase = () => {
  try {
    // Execute all table creation in a transaction
    db.withTransactionSync(() => {
      db.execSync(`
        CREATE TABLE IF NOT EXISTS users (
          id TEXT PRIMARY KEY NOT NULL,
          email TEXT UNIQUE NOT NULL,
          password TEXT NOT NULL,
          name TEXT NOT NULL,
          role TEXT NOT NULL
        );

        CREATE TABLE IF NOT EXISTS quizzes (
          id TEXT PRIMARY KEY NOT NULL,
          title TEXT NOT NULL,
          teacher_id TEXT NOT NULL,
          published INTEGER DEFAULT 0,
          created_at TEXT DEFAULT CURRENT_TIMESTAMP,
          FOREIGN KEY (teacher_id) REFERENCES users (id)
        );

        CREATE TABLE IF NOT EXISTS questions (
          id TEXT PRIMARY KEY NOT NULL,
          quiz_id TEXT NOT NULL,
          text TEXT NOT NULL,
          FOREIGN KEY (quiz_id) REFERENCES quizzes (id)
        );

        CREATE TABLE IF NOT EXISTS answers (
          id TEXT PRIMARY KEY NOT NULL,
          question_id TEXT NOT NULL,
          text TEXT NOT NULL,
          correct INTEGER DEFAULT 0,
          FOREIGN KEY (question_id) REFERENCES questions (id)
        );

        CREATE TABLE IF NOT EXISTS results (
          id TEXT PRIMARY KEY NOT NULL,
          quiz_id TEXT NOT NULL,
          student_id TEXT NOT NULL,
          score INTEGER NOT NULL,
          total_questions INTEGER NOT NULL,
          date TEXT DEFAULT CURRENT_TIMESTAMP,
          FOREIGN KEY (quiz_id) REFERENCES quizzes (id),
          FOREIGN KEY (student_id) REFERENCES users (id)
        );
      `);
    });
    console.log('Database initialized');
  } catch (error) {
    console.error('Error initializing database:', error);
    throw error;
  }
};

const executeQuery = (sql, params = []) => {
  try {
    const statement = db.prepareSync(sql);
    const result = params.length > 0 ? statement.executeSync(params) : statement.executeSync();
    statement.finalizeSync();
    return result;
  } catch (error) {
    console.error('Error executing query:', error);
    throw error;
  }
};

// User functions
const addUser = (user) => {
  const { id, email, password, name, role } = user;
  executeQuery(
    'INSERT INTO users (id, email, password, name, role) VALUES (?, ?, ?, ?, ?)',
    [id, email, password, name, role]
  );
  return user;
};

const getUserByEmail = (email) => {
  const result = executeQuery(
    'SELECT * FROM users WHERE email = ?',
    [email]
  );
  return result.rows?._array[0] || null;
};

// Quiz functions
const addQuiz = (quiz) => {
  const { id, title, teacherId, published } = quiz;
  executeQuery(
    'INSERT INTO quizzes (id, title, teacher_id, published) VALUES (?, ?, ?, ?)',
    [id, title, teacherId, published ? 1 : 0]
  );

  // Add questions and answers
  for (const question of quiz.questions) {
    const questionId = Math.random().toString(36).substring(7);
    executeQuery(
      'INSERT INTO questions (id, quiz_id, text) VALUES (?, ?, ?)',
      [questionId, id, question.text]
    );

    for (const answer of question.answers) {
      executeQuery(
        'INSERT INTO answers (id, question_id, text, correct) VALUES (?, ?, ?, ?)',
        [Math.random().toString(36).substring(7), questionId, answer.text, answer.correct ? 1 : 0]
      );
    }
  }

  return quiz;
};

const getQuizById = (id) => {
  const quizResult = executeQuery(
    'SELECT * FROM quizzes WHERE id = ?',
    [id]
  );
  const quiz = quizResult.rows?._array[0];
  if (!quiz) return null;

  // Get questions
  const questionsResult = executeQuery(
    'SELECT * FROM questions WHERE quiz_id = ?',
    [id]
  );
  const questions = questionsResult.rows?._array || [];

  // Get answers for each question
  for (const question of questions) {
    const answersResult = executeQuery(
      'SELECT * FROM answers WHERE question_id = ?',
      [question.id]
    );
    question.answers = (answersResult.rows?._array || []).map(a => ({
      id: a.id,
      text: a.text,
      correct: a.correct === 1
    }));
  }

  return {
    ...quiz,
    published: quiz.published === 1,
    questions: questions.map(q => ({
      id: q.id,
      text: q.text,
      answers: q.answers
    }))
  };
};

const getQuizzesByTeacher = (teacherId) => {
  const result = executeQuery(
    'SELECT * FROM quizzes WHERE teacher_id = ?',
    [teacherId]
  );
  return result.rows._array.map(q => ({
    ...q,
    published: q.published === 1
  }));
};

const getAllPublishedQuizzes = () => {
  const result = executeQuery(
    'SELECT * FROM quizzes WHERE published = 1'
  );
  return result.rows._array.map(q => ({
    ...q,
    published: q.published === 1
  }));
};

const updateQuiz = (quiz) => {
  const { id, title, published } = quiz;
  executeQuery(
    'UPDATE quizzes SET title = ?, published = ? WHERE id = ?',
    [title, published ? 1 : 0, id]
  );

  // Delete existing questions and answers
  executeQuery(
    'DELETE FROM answers WHERE question_id IN (SELECT id FROM questions WHERE quiz_id = ?)',
    [id]
  );
  executeQuery(
    'DELETE FROM questions WHERE quiz_id = ?',
    [id]
  );

  // Add new questions and answers
  for (const question of quiz.questions) {
    const questionId = Math.random().toString(36).substring(7);
    executeQuery(
      'INSERT INTO questions (id, quiz_id, text) VALUES (?, ?, ?)',
      [questionId, id, question.text]
    );

    for (const answer of question.answers) {
      executeQuery(
        'INSERT INTO answers (id, question_id, text, correct) VALUES (?, ?, ?, ?)',
        [Math.random().toString(36).substring(7), questionId, answer.text, answer.correct ? 1 : 0]
      );
    }
  }

  return quiz;
};

const deleteQuiz = (id) => {
  // First delete answers
  executeQuery(
    'DELETE FROM answers WHERE question_id IN (SELECT id FROM questions WHERE quiz_id = ?)',
    [id]
  );
  // Then delete questions
  executeQuery(
    'DELETE FROM questions WHERE quiz_id = ?',
    [id]
  );
  // Finally delete the quiz
  executeQuery(
    'DELETE FROM quizzes WHERE id = ?',
    [id]
  );
};

// Result functions
const addResult =   (result) => {
  const { quizId, studentId, score, totalQuestions } = result;
   executeQuery(
    'INSERT INTO results (id, quiz_id, student_id, score, total_questions) VALUES (?, ?, ?, ?, ?)',
    [Math.random().toString(36).substring(7), quizId, studentId, score, totalQuestions]
  );
  return result;
};

const getResultsByStudent =   (studentId) => {
  const result =  executeQuery(
    'SELECT * FROM results WHERE student_id = ?',
    [studentId]
  );
  return result.rows._array;
};

const initializeTestData = () => {
  try {
    // Check if we already have users
    const usersResult = executeQuery('SELECT COUNT(*) as count FROM users');
    if (usersResult.rows?._array[0]?.count === 0) {
      // Add test teacher
      addUser({
        id: '1',
        email: 'teacher@ecat.com',
        password: 'password123',
        name: 'Test Teacher',
        role: 'teacher'
      });

      // Add test student
      addUser({
        id: '2',
        email: 'student@ecat.com',
        password: 'password123',
        name: 'Test Student',
        role: 'student'
      });

      console.log('Test users added');
    }
  } catch (error) {
    console.log('Error initializing test data:', error);
  }
};

export default {
  initializeDatabase,
  initializeTestData,
  addUser,
  getUserByEmail,
  addQuiz,
  getQuizById,
  getQuizzesByTeacher,
  getAllPublishedQuizzes,
  updateQuiz,
  deleteQuiz,
  addResult,
  getResultsByStudent
};