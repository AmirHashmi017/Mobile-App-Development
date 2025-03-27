import { openDatabaseAsync } from 'expo-sqlite/next';

// Open database asynchronously
let db;

const initializeDatabase = async () => {
  try {
    db = await openDatabaseAsync('ecat_quiz.db');
    
    // Use transaction for all table creations
    await db.execAsync(`
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
    
    console.log('Database initialized');
  } catch (error) {
    console.error('Error initializing database:', error);
    throw error;
  }
};

const executeQuery = async (sql, params = []) => {
  if (!db) {
    throw new Error('Database not initialized');
  }

  try {
    // For SELECT queries
    if (sql.trim().toUpperCase().startsWith('SELECT')) {
      const result = await db.getAllAsync(sql, params);
      return { rows: { _array: result } };
    }
    
    // For INSERT/UPDATE/DELETE queries
    const result = await db.runAsync(sql, params);
    return result;
  } catch (error) {
    console.error('Error executing query:', error);
    throw error;
  }
};



// User functions
const addUser = async (user) => {
  const { id, email, password, name, role } = user;
  await executeQuery(
    'INSERT INTO users (id, email, password, name, role) VALUES (?, ?, ?, ?, ?)',
    [id, email, password, name, role]
  );
  return user;
};

const getUserByEmail = async (email) => {
  const result = await executeQuery(
    'SELECT * FROM users WHERE email = ?',
    [email]
  );
  return result.rows._array[0];
};

// Quiz functions
const addQuiz = async (quiz) => {
  const { id, title, teacherId, published } = quiz;
  await executeQuery(
    'INSERT INTO quizzes (id, title, teacher_id, published) VALUES (?, ?, ?, ?)',
    [id, title, teacherId, published ? 1 : 0]
  );

  // Add questions and answers
  for (const question of quiz.questions) {
    const questionId = Math.random().toString(36).substring(7);
    await executeQuery(
      'INSERT INTO questions (id, quiz_id, text) VALUES (?, ?, ?)',
      [questionId, id, question.text]
    );

    for (const answer of question.answers) {
      await executeQuery(
        'INSERT INTO answers (id, question_id, text, correct) VALUES (?, ?, ?, ?)',
        [Math.random().toString(36).substring(7), questionId, answer.text, answer.correct ? 1 : 0]
      );
    }
  }

  return quiz;
};

const getQuizById = async (id) => {
  const quizResult = await executeQuery(
    'SELECT * FROM quizzes WHERE id = ?',
    [id]
  );
  const quiz = quizResult.rows._array[0];
  if (!quiz) return null;

  // Get questions
  const questionsResult = await executeQuery(
    'SELECT * FROM questions WHERE quiz_id = ?',
    [id]
  );
  const questions = questionsResult.rows._array;

  // Get answers for each question
  for (const question of questions) {
    const answersResult = await executeQuery(
      'SELECT * FROM answers WHERE question_id = ?',
      [question.id]
    );
    question.answers = answersResult.rows._array.map(a => ({
      id: a.id,
      text: a.text,
      correct: a.correct === 1
    }));
  }

  return {
    ...quiz,
    published: quiz.published === 1,
    questions: questions.map(q => ({
      text: q.text,
      answers: q.answers
    }))
  };
};

const getQuizzesByTeacher = async (teacherId) => {
  const result = await executeQuery(
    'SELECT * FROM quizzes WHERE teacher_id = ?',
    [teacherId]
  );
  return result.rows._array.map(q => ({
    ...q,
    published: q.published === 1
  }));
};

const getAllPublishedQuizzes = async () => {
  const result = await executeQuery(
    'SELECT * FROM quizzes WHERE published = 1'
  );
  return result.rows._array.map(q => ({
    ...q,
    published: q.published === 1
  }));
};

const updateQuiz = async (quiz) => {
  const { id, title, published } = quiz;
  await executeQuery(
    'UPDATE quizzes SET title = ?, published = ? WHERE id = ?',
    [title, published ? 1 : 0, id]
  );

  // Delete existing questions and answers
  await executeQuery(
    'DELETE FROM answers WHERE question_id IN (SELECT id FROM questions WHERE quiz_id = ?)',
    [id]
  );
  await executeQuery(
    'DELETE FROM questions WHERE quiz_id = ?',
    [id]
  );

  // Add new questions and answers
  for (const question of quiz.questions) {
    const questionId = Math.random().toString(36).substring(7);
    await executeQuery(
      'INSERT INTO questions (id, quiz_id, text) VALUES (?, ?, ?)',
      [questionId, id, question.text]
    );

    for (const answer of question.answers) {
      await executeQuery(
        'INSERT INTO answers (id, question_id, text, correct) VALUES (?, ?, ?, ?)',
        [Math.random().toString(36).substring(7), questionId, answer.text, answer.correct ? 1 : 0]
      );
    }
  }

  return quiz;
};

const deleteQuiz = async (id) => {
  // First delete answers
  await executeQuery(
    'DELETE FROM answers WHERE question_id IN (SELECT id FROM questions WHERE quiz_id = ?)',
    [id]
  );
  // Then delete questions
  await executeQuery(
    'DELETE FROM questions WHERE quiz_id = ?',
    [id]
  );
  // Finally delete the quiz
  await executeQuery(
    'DELETE FROM quizzes WHERE id = ?',
    [id]
  );
};

// Result functions
const addResult = async (result) => {
  const { quizId, studentId, score, totalQuestions } = result;
  await executeQuery(
    'INSERT INTO results (id, quiz_id, student_id, score, total_questions) VALUES (?, ?, ?, ?, ?)',
    [Math.random().toString(36).substring(7), quizId, studentId, score, totalQuestions]
  );
  return result;
};

const getResultsByStudent = async (studentId) => {
  const result = await executeQuery(
    'SELECT * FROM results WHERE student_id = ?',
    [studentId]
  );
  return result.rows._array;
};

// Initialize with some test data
const initializeTestData = async () => {
  try {
    // Check if we already have users
    const usersResult = await executeQuery('SELECT COUNT(*) as count FROM users');
    if (usersResult.rows._array[0].count === 0) {
      // Add test teacher
      await addUser({
        id: '1',
        email: 'teacher@ecat.com',
        password: 'password123',
        name: 'Test Teacher',
        role: 'teacher'
      });

      // Add test student
      await addUser({
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