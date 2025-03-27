
import React, { useEffect } from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import { AuthProvider } from './context/AuthContext';
import database from './database/database';

import Login from './screens/Login';
import Signup from './screens/Signup';
import TeacherDashboard from './screens/TeacherDashboard';
import StudentDashboard from './screens/StudentDashboard';
import CreateQuiz from './screens/CreateQuiz';
import EditQuiz from './screens/EditQuiz';
import Quiz from './screens/Quiz';
import QuizResult from './screens/QuizResult';

const Stack = createStackNavigator();

const App = () => {
  useEffect(() => {
    const initializeApp = async () => {
      try {
        await database.initializeDatabase();
        await database.initializeTestData();
        console.log('Database initialized');
      } catch (error) {
        console.error('Initialization error:', error);
      }
    };
    initializeApp();
  }, []);

  return (
    <AuthProvider>
      <NavigationContainer>
        <Stack.Navigator>
          <Stack.Screen name="Login" component={Login} options={{ headerShown: false }} />
          <Stack.Screen name="Signup" component={Signup} options={{ headerShown: false }} />
          <Stack.Screen name="TeacherDashboard" component={TeacherDashboard} />
          <Stack.Screen name="StudentDashboard" component={StudentDashboard} />
          <Stack.Screen name="CreateQuiz" component={CreateQuiz} />
          <Stack.Screen name="EditQuiz" component={EditQuiz} />
          <Stack.Screen name="Quiz" component={Quiz} />
          <Stack.Screen name="QuizResult" component={QuizResult} options={{ headerShown: false }} />
        </Stack.Navigator>
      </NavigationContainer>
    </AuthProvider>
  );
};

export default App;