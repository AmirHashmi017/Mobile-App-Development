import React, { useEffect, useState } from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import { ActivityIndicator, View } from 'react-native';
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
  const [isReady, setIsReady] = useState(false);

  useEffect(() => {
    const initializeApp = () => {
      try {
        database.initializeDatabase();
        database.initializeTestData();
        console.log('Database initialized');
        setIsReady(true);
      } catch (error) {
        console.error('Initialization error:', error);
        setIsReady(true); 
      }
    };
    initializeApp();
  }, []);

  if (!isReady) {
    return (
      <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
        <ActivityIndicator size="large" />
      </View>
    );
  }

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