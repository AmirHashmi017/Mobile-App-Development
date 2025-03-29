let Users = []; 
let Quizzes = [];
let Questions = [];
let Answers = [];
let Results = [];

class User {
  constructor(id, name, email, password, role) {
    this.id = id;
    this.name = name; 
    this.email = email;
    this.password = password;
    this.role = role;
  }
}

class quizzes {
  constructor(id, title, teacher_id, published) {
    this.id = id;
    this.title = title;
    this.teacher_id = teacher_id;
    this.published = published;
    this.created_at = new Date();
  }
}

class questionsclass {
  constructor(id, quiz_id, text) {
    this.id = id;
    this.quiz_id = quiz_id;
    this.text = text;
  }
}

class answers {
  constructor(id, question_id, text, correct) {
    this.id = id;
    this.question_id = question_id;
    this.text = text;
    this.correct = correct;
  }
}

class results {
  constructor(id, quiz_id, student_id, score, total_questions) {
    this.id = id;
    this.quiz_id = quiz_id;
    this.student_id = student_id;
    this.score = score;
    this.total_questions = total_questions;
    this.date = new Date();
  }
}

const database = {
  addUser: (user) => {
    const { id, name, email, password, role } = user;
    const newUser = new User(id, name, email, password, role);
    Users.push(newUser); 
    return Promise.resolve(newUser);
  },

  getUserByEmail: (email) => {
    const user = Users.filter(user => user.email === email); 
    return Promise.resolve(user[0]);
  },

  addQuiz: (quiz) => {
    const { id, title, teacherId, published, questions } = quiz;
    const newQuiz = new quizzes(id, title, teacherId, published ? 1 : 0);
    Quizzes.push(newQuiz);
    
    for (const question of questions) {
      const questionId = Math.random().toString(36).substring(7);
      Questions.push(new questionsclass(questionId, id, question.text));

      for (const answer of question.answers) {
        const answerID = Math.random().toString(36).substring(7);
        Answers.push(new answers(answerID, questionId, answer.text, answer.correct ? 1 : 0));
      }
    }
    return Promise.resolve(newQuiz);
  },

  getQuizzesByTeacher: (teacherId) => {
    const result = Quizzes.filter(quiz => quiz.teacher_id === teacherId);
    return Promise.resolve(result.map(q => ({
      ...q,
      published: q.published === 1
    })));
  },

  updateQuiz: (quiz) => {
    const { id, title, published, questions } = quiz;

    const quizIndex = Quizzes.findIndex(q => q.id === id);
    if (quizIndex !== -1) {
      Quizzes[quizIndex] = {
        ...Quizzes[quizIndex],
        title,
        published: published ? 1 : 0
      };
    }
 
    const questionIds = Questions
      .filter(q => q.quiz_id === id)
      .map(q => q.id);
    
    Answers = Answers.filter(a => !questionIds.includes(a.question_id));
    Questions = Questions.filter(q => q.quiz_id !== id);

    questions.forEach(question => {
      const questionId = Math.random().toString(36).substring(7);
      Questions.push(new questionsclass(questionId, id, question.text));
  
      question.answers.forEach(answer => {
        Answers.push(new answers(
          Math.random().toString(36).substring(7),
          questionId,
          answer.text,
          answer.correct ? 1 : 0
        ));
      });
    });
  
    return Promise.resolve(quiz);
  },

  getAllPublishedQuizzes: () => {
    try {
      if (!Array.isArray(Quizzes)) {
        console.error('Quizzes is not an array:', Quizzes);
        return Promise.resolve([]);
      }

      const publishedQuizzes = Quizzes.filter(quiz => {
        return quiz && typeof quiz.published !== 'undefined' && quiz.published === 1;
      });
  
      if (!publishedQuizzes.length) {
        return Promise.resolve([]);
      }

      const result = publishedQuizzes.map(quiz => {
        const quizQuestions = Questions.filter(question => 
          question && question.quiz_id === quiz.id
        );
  
        const questionsWithAnswers = quizQuestions.map(question => {
          const questionAnswers = Answers.filter(answer =>
            answer && answer.question_id === question.id
          ).map(answer => ({
            ...answer,
            correct: answer.correct === 1
          }));
  
          return {
            ...question,
            answers: questionAnswers || []
          };
        });
  
        return {
          ...quiz,
          published: quiz.published === 1,
          questions: questionsWithAnswers || []
        };
      });
  
      return Promise.resolve(result);
    } catch (error) {
      console.error('Error in getAllPublishedQuizzes:', error);
      return Promise.resolve([]);
    }
  },
  
  getQuizById: (id) => {
    const quiz = Quizzes.find(q => q.id === id);
    if (!quiz) return Promise.resolve(null);
  
    const questions = Questions
      .filter(q => q.quiz_id === id)
      .map(question => {
        const answers = Answers
          .filter(a => a.question_id === question.id)
          .map(answer => ({
            ...answer,
            correct: answer.correct === 1 
          }));
  
        return {
          ...question,
          answers
        };
      });
  
    return Promise.resolve({
      ...quiz,
      published: quiz.published === 1,
      questions
    });
  },
  
  deleteQuiz: (id) => {
    const questionIds = Questions
      .filter(q => q.quiz_id === id)
      .map(q => q.id);
    
    Answers = Answers.filter(a => !questionIds.includes(a.question_id));
    Questions = Questions.filter(q => q.quiz_id !== id);
    Quizzes = Quizzes.filter(q => q.id !== id);
    
    return Promise.resolve();
  },

  addResult: (result) => {
    const { quizId, studentId, score, totalQuestions } = result;

    const newResult = new results(
      Math.random().toString(36).substring(7),
      quizId,
      studentId,
      score,
      totalQuestions
    );

    Results.push(newResult);
    
    return Promise.resolve(result); 
  },
  
  getResultsByStudent: (studentId) => {
    const studentResults = Results.filter(result => result.student_id === studentId);
    
    return Promise.resolve(studentResults); 
  }
};

export default database;