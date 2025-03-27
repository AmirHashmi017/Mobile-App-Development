// src/screens/StudentDashboard.js
import React, { useState, useEffect, useContext } from 'react';
import { View, Text, TouchableOpacity, FlatList, StyleSheet, Alert } from 'react-native';
import database from '../database/database';
import { AuthContext } from '../context/AuthContext';

const StudentDashboard = ({ navigation }) => {
  const [quizzes, setQuizzes] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const { user, logout } = useContext(AuthContext);

  useEffect(() => {
    const loadQuizzes = async () => {
      setIsLoading(true);
      try {
        const publishedQuizzes = await database.getAllPublishedQuizzes();
        setQuizzes(publishedQuizzes);
      } catch (error) {
        Alert.alert('Error', 'Failed to load quizzes');
        console.error(error);
      } finally {
        setIsLoading(false);
      }
    };
    loadQuizzes();
  }, []);

  const handleAttemptQuiz = (quiz) => {
    navigation.navigate('Quiz', { 
      questions: quiz.questions,
      title: quiz.title,
      color: '#36B1F0',
      quizId: quiz.id
    });
  };

  const handleLogout = () => {
    logout();
    navigation.navigate('Login');
  };

  React.useLayoutEffect(() => {
    navigation.setOptions({
      headerRight: () => (
        <TouchableOpacity onPress={handleLogout} style={styles.logoutButton}>
          <Text style={styles.logoutText}>Logout</Text>
        </TouchableOpacity>
      ),
    });
  }, [navigation]);

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Available Quizzes</Text>
      
      {isLoading ? (
        <Text style={styles.loadingText}>Loading quizzes...</Text>
      ) : (
        <FlatList
          data={quizzes}
          keyExtractor={(item) => item.id}
          renderItem={({ item }) => (
            <TouchableOpacity 
              style={styles.quizCard}
              onPress={() => handleAttemptQuiz(item)}
            >
              <Text style={styles.quizTitle}>{item.title}</Text>
              <Text style={styles.quizInfo}>
                {item.questions.length} question{item.questions.length !== 1 ? 's' : ''}
              </Text>
              {item.teacherName && (
                <Text style={styles.quizTeacher}>By: {item.teacherName}</Text>
              )}
            </TouchableOpacity>
          )}
          ListEmptyComponent={
            <Text style={styles.emptyText}>No quizzes available yet</Text>
          }
        />
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    backgroundColor: '#f5f5f5',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 20,
    textAlign: 'center',
  },
  quizCard: {
    backgroundColor: '#fff',
    padding: 15,
    borderRadius: 5,
    marginBottom: 15,
    borderWidth: 1,
    borderColor: '#ddd',
  },
  quizTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 5,
  },
  quizInfo: {
    color: '#666',
    marginBottom: 3,
  },
  quizTeacher: {
    color: '#888',
    fontSize: 14,
    fontStyle: 'italic',
  },
  logoutButton: {
    marginRight: 15,
  },
  logoutText: {
    color: '#ff4136',
    fontWeight: 'bold',
  },
  loadingText: {
    textAlign: 'center',
    marginTop: 20,
    color: '#666',
  },
  emptyText: {
    textAlign: 'center',
    marginTop: 20,
    color: '#666',
    fontSize: 16,
  },
});

export default StudentDashboard;